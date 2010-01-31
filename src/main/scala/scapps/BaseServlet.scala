package scapps

import RichRequest._
import Scapps._
import scalaz._
import Scalaz._
import scalaz.http.servlet._
import scalaz.http.request._
import scalaz.http.response._

abstract class BaseServlet extends ServletApplicationServlet[Stream,Stream] {
  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
    scapps.R.service(request, servletRequest.session) {
      val a = arounds.∑
      a {
        request.log
        route(request.methodHax()) | _404_
      }
    }
  }
    
//  def gae[T]: Ar = Services.service(_)
  
  def arounds: List[Around[Response[Stream]]]
  
  val route: Request[Stream] => Option[Response[Stream]]
  
  def _404_ : Response[Stream]
}