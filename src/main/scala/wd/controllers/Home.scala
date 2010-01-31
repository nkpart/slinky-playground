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

abstract class BaseServlet extends ServletApplicationServlet[Stream,Stream] {
  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
    R.service(request, servletRequest.session) {
      Services.service {
        request.log
        route(request.methodHax()) | NotFound.xhtml
      }
    }
  }
  
  def route: Request[Stream] => Option[Response[Stream]]
  
  def _404_ : Response[Stream]
}

final class WorthDrinkingServlet extends ServletApplicationServlet[Stream, Stream] {
  val route: Request[Stream] => Option[Response[Stream]] = {
    import Services._
    
    check(slinky.isLoggedIn, slinky.doLogin()) {
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

