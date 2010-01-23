import com.google.appengine.api.datastore._
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae._
import scala.collection.JavaConversions._

package gae {
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
  trait EntityCreatable[T] { def create(e: Entity): T }

  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }
  
  case class Keyed[T](value: T,key: Key) {
    def save(ds: DatastoreService)(implicit keyFor: KeyFor[T], ew: EntityWriteable[T]): Keyed[T] = {
      val e = keyFor.createEntity(value, None)
      ew.write(value, e)
      Keyed(value, ds.put(e))
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
}

package object gae {
  implicit def identityTo[T](t: T) = new GaeIdentity[T] { val value = t }
  implicit def identityFrom[T](t: GaeIdentity[T]): T = t.value
  implicit def richUserServiceTo(us: UserService): RichUserService = RichUserService(us)

  implicit def richUserServiceFrom(us: RichUserService): UserService = us.us

  def userService = UserServiceFactory.getUserService

  def classKind[T](implicit m: ClassManifest[T]): String = m.erasure.getName

  def createQuery[T](implicit s: Kind[T]) = new Query(s.kind)
  
  def query[T](ds: DatastoreService)(f: Query => Query)(implicit s: Kind[T]): Iterable[Entity] = {
    val qry = createQuery[T]
    ds.prepare(f(qry)).asIterable
  }
  
  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


