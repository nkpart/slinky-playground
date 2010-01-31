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
import scalaz._
import rest._
import views._

import scapps.RichRequest._
import scapps._
import Scapps._

final class WorthDrinkingServlet extends BaseServlet {
  import scapps.R._
  def _404_ = NotFound.xhtml
  
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
  
  def arounds = List(Services.service _)

  override def init() = {
    prohax.Bootstrap.defineInflections_!
  }
}

