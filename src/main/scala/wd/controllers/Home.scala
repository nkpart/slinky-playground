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
import com.google.appengine.api.datastore.{DatastoreService}
import scalaz.{Index => _}
import scapps.Scapps._
import rest._
import views._

import scapps.RichRequest._
import scapps._

case class Start(ds: DatastoreService) extends BaseController {
  def root(implicit request: Request[Stream]) = {
    val v = Brewery.allByName ∘ { breweries =>
        render(start.index(breweries))
    }
    Database.runDb(v) 
  }
  
  def config(implicit request: Request[Stream]) = {
    val styles = List(Style("a"))
    render(start.config(styles))
  }
}

final class WorthDrinkingServlet extends ServletApplicationServlet[Stream, Stream] {
  def userService = UserServiceFactory.getUserService

  val loggedIn = ☆(userService.currentUser >| (_:Request[Stream]))
  
  val login = ((r: Request[Stream]) => redirectTo(userService.createLoginURL("/"))(r)).kleisli[Option]
  
  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)
  
  val route: Request[Stream] => Option[Response[Stream]] = {
    import Database._
    
    check(loggedIn, login) {
      reduce(List(
        at(Nil) >=> m(GET) map (r => runDb(ds => Start(ds).root(r))),
        at("config") >=> m(GET) map (r => runDb(ds => Start(ds).config(r))),
        "beers" / (r => v => runDb { ds => new BeersController(ds)(r).handle(v)}),
        "breweries" / (r => v => runDb { ds => new BreweriesController(ds)(r).handle(v)})
      ))
    }
  }

  override def init() = {
    prohax.Bootstrap.defineInflections_!
  }
  
  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
    request.log
    route(request.methodHax()) | NotFound.xhtml
  }
}

