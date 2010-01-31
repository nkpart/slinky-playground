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

final class WorthDrinkingServlet extends BaseServlet with BaseController {
  import scapps.R._
  import Services._
  def _404_ = render(NotFound, <p>404</p>)
  def _503_ = render(Forbidden, <p>503</p>)
  
  implicit def richAction(v: Action[String]) = new {
    def lookup[T](implicit ds: DatastoreService, m: Model[T]) = v ↦ (key => ds.findById[T](key.toLong))
  }
  
  val route: Request[Stream] => Option[Response[Stream]] = {
    check(slinky.isLoggedIn, slinky.doLogin()) {
      reduce(List(
        at(Nil) >=> m(GET) map (r => Start.root),
        at("config") >=> m(GET) map slinky.adminOnly(_ => Start.config, _503_),
        "beers" / (r => v => BeersController(v)),
        "breweries" / (r => v => {
          v.lookup[Brewery] >>= Breweries.apply _
        })
      ))
    }
  }
  
  def arounds = List(Services.service _)

  override def init = prohax.Bootstrap.defineInflections_!
}

