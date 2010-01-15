package wd
package controllers

import scalaz._
import Scalaz._
import scapps.Controller
import scalaz.http.response.{Response, NotFound}
import scalaz.http.request.Request
import wd.views.layouts
import com.google.appengine.api.users.UserServiceFactory

trait ControllerHelpers {
  self: Controller =>
  
  implicit val charset = UTF8
  
  val layout = layouts.main(UserServiceFactory.getUserService) _

  def fourOhFour(request: Request[Stream]) = render(NotFound, <p>404</p>)(request)

  def or404(o: Option[Response[Stream]])(implicit r: Request[Stream]) = o getOrElse fourOhFour(r)

  implicit def ResponseZero(implicit r: Request[Stream]): Zero[Response[Stream]] = new Zero[Response[Stream]] {
    val zero = fourOhFour(r)
  }
}

trait BaseController extends Controller with ControllerHelpers
