package wd
package controllers

import gae._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.request.Request._
import scalaz.http.servlet._
import scalaz.http.servlet.HttpServlet._
import scalaz.http.Slinky._
import scalaz.http.response._
import scalaz.http.response.Response._
import com.google.appengine.api.users._
import com.google.appengine.api.datastore._
import scalaz.{Index => _}
import scapps.Scapps._
import rest._
import views._

import scapps.RichRequest._
import scapps._

final class WorthDrinkingServlet extends ServletApplicationServlet[Stream, Stream] {
  def userService = UserServiceFactory.getUserService

  val loggedIn = ☆(userService.currentUser >| (_:Request[Stream]))
  
  val login = ((r: Request[Stream]) => redirectTo(userService.createLoginURL("/"))(r)).kleisli[Option]
  
  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)
  
  val route: Request[Stream] => Option[Response[Stream]] = {
    import Services._
    
    check(loggedIn, login) {
      reduce(List(
        at(Nil) >=> m(GET) map (r => Start.root),
        at("config") >=> m(GET) map (r => Start.config),
        "beers" / (r => v => BeersController.handle(v)),
        "breweries" / (r => v => BreweriesController.handle(v))
      ))
    }
  }

  override def init() = {
    prohax.Bootstrap.defineInflections_!
  }
  
  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
    R.service(request, servletRequest.session) {
      Services.service {
        request.log
        route(request.methodHax()) | NotFound.xhtml
      }
    }
  }
}

