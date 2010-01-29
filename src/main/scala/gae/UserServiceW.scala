package gae

import com.google.appengine.api.users._
import scalaz._
import Scalaz._

trait UserServiceW {
  val us: UserService
  def currentUser: Option[User] = Option(us.getCurrentUser)
}

trait UserServiceImplicits {
  implicit def userServiceTo(s: UserService): UserServiceW = new UserServiceW { val us = s }
  implicit def userServiceFrom(us: UserServiceW): UserService = us.us  
}