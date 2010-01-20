import com.google.appengine.api.datastore._
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae._

package gae {
  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }
}

package object gae {

  implicit def richUserServiceTo(us: UserService): RichUserService = RichUserService(us)

  implicit def richUserServiceFrom(us: RichUserService): UserService = us.us

  def userService = UserServiceFactory.getUserService

  def classKind[T](implicit m: ClassManifest[T]): String = m.erasure.getName

  def createQuery[T](implicit s : Stored[T]) = new Query(s.kind)
  
  def createKey[T](name: String)(implicit s : Stored[T]) = KeyFactory.createKey(s.kind, name)

  def entity[T](t: T)(implicit ew: EntityWriteable[T], stored: Stored[T]) = {
    val e = new Entity(stored.kind, stored.keyName(t))
    ew.write(t, e)
    e
  }

  def parentedEntity[T](t: T, parentKey: Key)(implicit ew: EntityWriteable[T], stored: Stored[T]) = {
    val e = new Entity(stored.kind, stored.keyName(t), parentKey)
    ew.write(t, e)
    e
  }

  private def f[T](e: Entity, t: T, ds: DatastoreService)(implicit stored: Stored[T]) = {
    val rk = ds.put(e)
    stored.withKey(t, rk)
  }
  
  def persistWithParent[T](t: T, parentKey: Key)(ds : DatastoreService)(implicit ew: EntityWriteable[T], stored: Stored[T]): T = {
    f(parentedEntity(t, parentKey), t, ds)
  }

  def persist[T](t: T)(ds : DatastoreService)(implicit ew: EntityWriteable[T], stored: Stored[T]): T = {
    f(entity(t), t, ds)
  }
}


