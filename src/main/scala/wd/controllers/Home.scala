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
import scalaz._
import scapps.Scapps._
import views._

import scapps.RichRequest._
import scapps.{RestfulActions, Controller}

class StartController(implicit val request: Request[Stream]) extends BaseController {
  override def index = Some {
    val v = (Brewery.allByName _) ∘ { breweries =>
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

  def route(r: Request[Stream]) = {
    val app = check(☆(admin _), login) {
      reduce(List(
        at(Nil) >=> m(GET) >=> (r => new StartController()(r).index),
        RestfulActions.mount("beers", {new BeersController()(_)}),
        RestfulActions.mount("breweries", {new BreweriesController()(_)})
        ))
    }
    app(r)
  }
}

