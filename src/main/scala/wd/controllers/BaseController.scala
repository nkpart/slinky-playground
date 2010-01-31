package wd
package controllers

import scalaz._
import Scalaz._
import scapps.Controller
import scalaz.http.response.{Response, NotFound}
import scalaz.http.request.Request
import wd.views.layouts
import com.google.appengine.api.users.UserServiceFactory

trait ControllerHelpers { self: Controller =>
  implicit val charset = UTF8
  
  val layout = layouts.main(UserServiceFactory.getUserService) _
}

trait BaseController extends Controller with ControllerHelpers

trait RestController[T] extends BaseController {
  def apply(v: rest.Action[T]): Option[Response[Stream]]
}
