import com.google.appengine.api.datastore.{KeyFactory, Query}
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae.{Kind, RichUserService}

package gae {
  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }
}

package object gae {
  implicit def richUserServiceTo(us: UserService): RichUserService = RichUserService(us)

  implicit def richUserServiceFrom(us: RichUserService): UserService = us.us

  def userService = UserServiceFactory.getUserService

  def classKind[T](implicit m: ClassManifest[T]): Kind[T] = new Kind[T] {val kind = m.erasure.getName}

  def createQuery[T](implicit k : Kind[T]) = new Query(k.kind)
  
  def createKey[T](name: String)(implicit k : Kind[T]) = KeyFactory.createKey(k.kind, name)
}


