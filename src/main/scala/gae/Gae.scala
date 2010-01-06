import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae.RichUserService


package gae {
  case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
  }
}

package object gae {
  implicit def richUserServiceTo(us: UserService): RichUserService = RichUserService(us)

  implicit def richUserServiceFrom(us: RichUserService): UserService = us.us

  def userService = UserServiceFactory.getUserService
}


