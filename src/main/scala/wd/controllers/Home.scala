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
import Scapps._
import views._

import RichRequest._

trait BaseController extends DefaultRestfulRoute with Controller {
  // TODO consider whether request should be an implicit value on here. would clean up actions a bit
  implicit val charset = UTF8
  val layout = layouts.main(userService) _

  def fourOhFour(request: Request[Stream]) = render(NotFound, <p>404</p>)(request)

  def or404(o: Option[Response[Stream]])(r: => Request[Stream]) = o getOrElse fourOhFour(r) 
}

class StartController extends BaseController {
  override def index(request: Request[Stream]) = Some {
    val v = (Brewery.allByName _) ∘ { breweries =>
      render(start.index(breweries))(request)
    }
    Database.runDb(v)
  }
}

class BreweriesController extends BaseController {
  override def show(request: Request[Stream]) = Some {

    val keyName = request("id").get
    val v: Option[Brewery] = Database.runDb(Brewery.findByKeyName(keyName) _);
    val r = v ∘ { brewery =>
      val beers = Database.runDb(brewery.beers _)
      render(breweries.show(brewery, beers))(request)
    }
    or404(r)(request)
  }

  override def edit(request: Request[Stream]) = Some {
    val keyName = request("id").get
    val v = (Brewery.findByKeyName(keyName) _) ∘∘ { (brewery: Brewery) =>
      render(breweries.edit(brewery))(request)
    }
    Database.runDb(v ∘ { or404(_)(request) })
  }

  override def create(request: Request[Stream]) = Some {
    val v = request("name") ∘ (name => Brewery(name, none).persist _)
    v map (Database.runDb(_): Brewery) map {
      v => redirectTo("/")(request)
    } getOrElse {
      render(<p>No name.</p>)(request)
    }
  }

  override def nnew(request: Request[Stream]) = Some {
    render(breweries.nnew)(request)
  }
}

class BeersController extends BaseController {
  override def nnew(request: Request[Stream]) = Some {
    val v = (Brewery.all _) ∘ (bs => render(beers.nnew(bs))(request))
    Database.runDb(v)
  }

  override def create(request: Request[Stream]) = Some {
    val name = request("name")
    val breweryKey = request("brewery")
    render {
      <div>
        <p>{name}</p>
      </div>
    }(request)
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

  def route = {
    val start = new StartController
    val breweries = new BreweriesController
    val beers = new BeersController

    val app = check(☆(admin _), login) {
      reduce(List(
        at(Nil) >=> m(GET) >=> (start.index _),
        beers.mountByName,
        breweries.mountByName
        ))
    }
    app
  }
}

