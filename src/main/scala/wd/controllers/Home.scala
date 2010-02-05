package wd
package controllers

import gae._
import rest._
import scapps._
import Scapps._
import scalaz._
import Scalaz._
import http.request._
import http.response._
import scalaz.http.Slinky._

import com.google.appengine.api.datastore._

final class WorthDrinkingServlet extends BaseServlet with WDLayout {
  import scapps.R._
  import Services._
  def _404_ = render(<p>404</p>, status = NotFound)
  def _503_ = render(<p>503</p>, status = Forbidden)
  
  implicit def richAction(v: Action[String]) = new {
    def lookup[T](base: sage.EntityBase[T])(implicit ds: DatastoreService) = v ↦ (key => base.lookup(key.toLong)(ds))
  }
  
  val route: Request[Stream] => Option[Response[Stream]] = {
    check(slinky.isLoggedIn, slinky.doLogin()) {
      reduce(List(
        at(Nil) >=> m(GET) map (r => Start.root),
        at("config") >=> m(GET) map slinky.adminOnly(_ => Start.config, _503_),
        "beers" / (r => v => BeersController(v)),
        "breweries" / (r => v => {
          v.lookup(Breweries) >>= BreweriesController.apply _
        })
      ))
    }
  }
  
  def arounds = List(Services.service _)

  override def init = prohax.Bootstrap.defineInflections_!
}

