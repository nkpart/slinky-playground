package wd.controllers

import scapps.{Controller, RestfulActions}
import scalaz.http.response.{Response, NotFound}
import scalaz.http.request.Request
import wd.views.layouts

trait BaseController extends RestfulActions with Controller {
  implicit val charset = UTF8
  val layout = layouts.main(userService) _

  def fourOhFour(request: Request[Stream]) = render(NotFound, <p>404</p>)(request)

  def or404(o: Option[Response[Stream]])(implicit r: Request[Stream]) = o getOrElse fourOhFour(r)
}
