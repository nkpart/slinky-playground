package belt

import gae._
import scalaz._
import Scalaz._
import scalaz.http.response._
import scalaz.http.request._
import rest._
import com.google.appengine.api.datastore._
import scapps._

import wd._
import controllers._

class Application extends Belt with WDLayout {
  prohax.Bootstrap.defineInflections_!

  def service(request: Request): Response = {
    scapps.Global.set(request)
    Services.setup_!
    request.log
    println(request.underlying.headers map {
      case (h, v) => "%s -> %s" format (h.asString, v.list.mkString)
    })
    
    Response(Unauthorized, (WWWAuthenticate, "Basic realm=\"Secure Area\""))(resp => {
      resp
    })
    try {
      route(request.methodHax()) | _404_
    } catch { case _ => _404_ }
  }

  import Services._
  def _404_ = render(<p>404</p>, status = NotFound)

  def _503_ = render(<p>503</p>, status = Forbidden)

  implicit def richAction(v: Action[String]) = new {
    def lookup[T](base: sage.EntityBase[T])(implicit ds: DatastoreService) = v ↦ (key => base.lookup(key.toLong)(ds))
  }

  val route: Request => Option[Response] = {
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
}
