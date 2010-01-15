package wd
package controllers

import gae._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.request.Request._
import scalaz.http.response._
import scalaz.http.response.Response._
import xml.NodeSeq
import com.google.appengine.api.users.{UserServiceFactory}
//import scalaz._
import scalaz.{Index => _}
import scapps.Scapps._
import views._

import scapps.RichRequest._
import scapps._

class StartController(implicit val request: Request[Stream]) extends BaseController {
  def go = {
    val v = (Brewery.allByName _) ∘ {
      breweries =>
        render(start.index(breweries))
    }
    Database.runDb(v)
  }
}

object Home {
  def userService = UserServiceFactory.getUserService

  def admin(request: Request[Stream]): Option[Request[Stream]] = userService.currentUser >| request

  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)

  val login = ((r: Request[Stream]) => redirectTo({
    println(userService.createLoginURL("/"))
    userService.createLoginURL("/")
  })(r)).kleisli[Option]

  def resource(base: String, f: (Request[Stream] => scapps.Verb => Option[Response[Stream]])) = ☆((r: Request[Stream]) => {
    println(r.action)
    r.action match {
      case Some(Action(b, verb)) if b == base  => f(r)(verb)
      case _ => none
    }
  })
  
  def route(r: Request[Stream]) = {
    val app = check(☆(admin _), login) {
      reduce(List(
        at(Nil) >=> m(GET) >=> (r => Some(new StartController()(r).go)),
        resource("beers", (r => v => new BeersController()(r).handle(v))),
        resource("breweries", (r => v => new BreweriesController()(r).handle(v)))
        ))
    }
    app(r)
  }
}

