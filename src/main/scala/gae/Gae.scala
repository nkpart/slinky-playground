import com.google.appengine.api.datastore._
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae._

package gae {
  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }
  
  trait GaeIdentity[T] {
    val value: T
    
    def persist(ds: DatastoreService)(implicit ew: EntityWriteable[T], stored: KeyFor[T]) = {
      val newKey = ds.put(entity(value,value))
      stored.withKey(value, newKey)
    }
    
    private def entity[KT, VT](kt: KT, vt: VT)(implicit key: KeyFor[KT], ew: EntityWriteable[VT])= {
      val e = key.entity(kt)
      ew.write(vt, e)
      e
    }
    
    def persistWithKey[KT](k: KT)(ds : DatastoreService)(implicit kf: KeyFor[KT], ew: EntityWriteable[T]) = {
      val nk = ds.put(entity(k, value))
      implicitly[KeyFor[KT]].withKey(k, nk)
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

  def createQuery[T](implicit s : Kind[T]) = new Query(s.kind)
  
  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


