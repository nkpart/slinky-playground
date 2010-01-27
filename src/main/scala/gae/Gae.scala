import com.google.appengine.api.datastore._
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae._

import scalaz._
import Scalaz._

package gae {
import wd.Beer

  trait Kind[T] {
    def kind: String
  }

  // Something that can be stored.
  trait KeyFor[T] extends Kind[T] {
    def keyName(t: T): Option[String]

    def createEntity(t: T, parentKey: Option[Key]): Entity = {
      (keyName(t), parentKey) match {
        case (Some(n), Some(pk)) => new Entity(kind, n, pk)
        case (Some(n), None) => new Entity(kind, n)
        case (None, Some(pk)) => new Entity(kind, pk)
        case (None, None) => new Entity(kind)
      }
    }
  }

  trait EntityWriteable[T] { def write(t: T, e: Entity) }
  trait EntityCreatable[T] { def createFrom(e: Entity): Option[T] }

  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }

  case class Keyed[T](value: T,key: Key) {
    def save(ds: DatastoreService)(implicit ew: EntityWriteable[T]): Keyed[T] = {
      val e = new Entity(key)
      ew.write(value, e)
      Keyed(value, ds.put(e))
    }

    def children[T](ds: DatastoreService)(implicit ec: EntityCreatable[T], k: Kind[T]): Iterable[Keyed[T]] = {
      ds.query[T] { qry =>
        qry.setAncestor(this.key)
      }
    }
  }

  trait GaeIdentity[T] {
    val value: T

    def insert(ds: DatastoreService)(implicit ew: EntityWriteable[T], stored: KeyFor[T]): Keyed[T] = {
      val newKey = ds.put(entity(value,value, None))
      Keyed(value, newKey)
    }

    def insertWithParent(parentKey: Key, ds: DatastoreService)(implicit ew: EntityWriteable[T], stored: KeyFor[T]): Keyed[T] = {
      val newKey = ds.put(entity(value,value, Some(parentKey)))
      Keyed(value, newKey)
    }

    private def entity[KT, VT](kt: KT, vt: VT, parentKey: Option[Key])(implicit key: KeyFor[KT], ew: EntityWriteable[VT])= {
      val e = key.createEntity(kt, parentKey)
      ew.write(vt, e)
      e
    }
  }

  trait RichEntity {
    val entity: Entity

    def create[T](implicit ec: EntityCreatable[T]): Option[Keyed[T]] = {
      Option(entity.getKey) >>= { key => ec.createFrom(entity) map { t => Keyed(t, key) } }
    }
  }

  trait RichDatastoreService {
    val ds: DatastoreService

    def all[T](implicit k : Kind[T], ec: EntityCreatable[T]) : Iterable[Keyed[T]] = {
      val es: Iterable[Entity] = ds.prepare(createQuery[T]).asIterable
      es flatMap (e => (entityTo(e).create[T]))
    }

    def findById[T](id: Long)(implicit k: Kind[T], ec: EntityCreatable[T]): Option[Keyed[T]] = {
      val key = createKey[T](id)
      Option(ds.get(key)) >>= (entityTo(_).create[T])
    }

    def query[T](f: Query => Query)(implicit s: Kind[T], ec: EntityCreatable[T]): Iterable[Keyed[T]] = {
      val qry = createQuery[T]
      ds.prepare(f(qry)).asIterable flatMap (e => e.create[T])
    }
  }
}

package object gae {
  implicit def identityTo[T](t: T) = new GaeIdentity[T] { val value = t }
  implicit def identityFrom[T](t: GaeIdentity[T]): T = t.value
  implicit def datastoreTo[T](d: DatastoreService): RichDatastoreService = new RichDatastoreService { val ds = d }
  implicit def datastoreFrom[T](t: RichDatastoreService): DatastoreService = t.ds
  implicit def entityTo(en: Entity) = new RichEntity { val entity = en }
  implicit def entityFrom(re: RichEntity) = re.entity

  implicit def richUserServiceTo(us: UserService): RichUserService = RichUserService(us)
  implicit def richUserServiceFrom(us: RichUserService): UserService = us.us

  def userService = UserServiceFactory.getUserService

  def classKind[T](implicit m: ClassManifest[T]): String = m.erasure.getName

  def createQuery[T](implicit s: Kind[T]) = new Query(s.kind)

  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


