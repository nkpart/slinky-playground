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

import RichRequest._

trait BaseController extends DefaultRestfulRoute with Controller {
  // TODO consider whether request should be an implicit value on here. would clean up actions a bit
  implicit val charset = UTF8
  val layout = layouts.main(userService) _
}

class StartController extends BaseController {
  override def index(request: Request[Stream]) = Some {
    val v = (Brewery.byName _) ∘ { breweries =>
      render(partials.index(breweries))(request)
    }
    Database.run(v)
  }
}

class BreweriesController extends BaseController {
  override def nnew(request: Request[Stream]) = Some {
    render(partials.newBreweryForm)(request)
  }

  override def create(request: Request[Stream]) = Some {
    val v = request("name") ∘ (name => Brewery(name, none).persist _)
    v map (Database.run(_): Brewery) map {
      v => redirectTo("/")(request)
    } getOrElse {
      render(<p>No name.</p>)(request)
    }
  }
}

class BeersController extends BaseController {
  override def nnew(request: Request[Stream]) = Some {
    val v = (Brewery.all _) ∘ (bs => render(partials.newBeerForm(bs))(request))
    Database.run(v)
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

