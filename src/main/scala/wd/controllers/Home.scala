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
import com.google.appengine.api.users.{UserServiceFactory}
import scalaz.{Index => _}
import scapps.Scapps._
import rest._
import views._

import scapps.RichRequest._
import scapps._

object Start extends BaseController {
  def go(implicit request: Request[Stream]) = {
    val v = (Brewery.allByName _) ∘ { breweries =>
        render(start.index(breweries))
    }
    Database.runDb(v)
  }
}

final class WorthDrinkingServlet extends ServletApplicationServlet[Stream, Stream] {
  def userService = UserServiceFactory.getUserService

  def admin(request: Request[Stream]): Option[Request[Stream]] = userService.currentUser >| request

  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)

  val login = ((r: Request[Stream]) => redirectTo(userService.createLoginURL("/"))(r)).kleisli[Option]

  def resource(base: String, f: (Request[Stream] => Action[String] => Option[Response[Stream]])) = ☆((r: Request[Stream]) => {
    r.action match {
      case Some((b, action)) if b == base => f(r)(action)
      case _   => none
    }
  })

  def route(r: Request[Stream]): Option[Response[Stream]] = {
    val app = check(☆(admin _), login) {
      reduce(List(
        at(Nil) >=> m(GET) >=> (r => Some(Start.go(r))),
        resource("beers", (r => v => Database.runDb { ds => new BeersController(ds)(r).handle(v)})),
        resource("breweries", (r => v => Database.runDb { ds => new BreweriesController(ds)(r).handle(v)}))
        ))
    }
    app(r)
  }

  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
    println(MethodParts.unapply(request))
    route(Scapps.methodHax[Stream].apply(request)) getOrElse HttpServlet.resource(x => OK << x.toStream, NotFound.xhtml)
  }
}

