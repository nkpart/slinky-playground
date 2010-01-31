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

  override def init = prohax.Bootstrap.defineInflections_!

}

