package gae

import com.google.appengine.api.users.{UserService, UserServiceFactory, User}

private[gae] case class RichUserService(us: UserService) {
    def currentUser: Option[User] = Option(us.getCurrentUser)
}

object Gae {
  implicit def richUserServiceTo(us : UserService) : RichUserService = RichUserService(us)
  implicit def richUserServiceFrom(us : RichUserService) : UserService = us.us
  
  def userService = UserServiceFactory.getUserService
}