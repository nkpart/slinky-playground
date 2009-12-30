package wd
package controllers

import gae.Gae._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.request.Request._
import scalaz.http.response._
import scalaz.http.response.Response._
import xml.NodeSeq
import Kinds._
import com.google.appengine.api.users.{UserServiceFactory}
import scalaz._
import Scapps._

trait MyWeb extends Web[Stream] {
  implicit val charset = UTF8
  def userService = UserServiceFactory.getUserService
  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)
}

trait BaseController extends DefaultRestfulRoute with MyWeb {
  implicit val layout = layouts.main(userService)
}

class StartController extends BaseController {
  override def index(request: Request[Stream]) = Some {
    val v = for (
      breweries <- Brewery.all _;
      beers <- Beer.all _
    ) yield {
        render(<p>{(request ! "k") ∘ (_.mkString)}</p><div>{partials.index}</div>)(request)
      }
    Database.run(v)    
  }
}

class BreweriesController extends BaseController {
  override def nnew(request: Request[Stream]) = Some {
    render(partials.newBreweryForm)(request)
  }
  
  override def create(request: Request[Stream]) = Some {
    val nm = (request | "name") map (_.mkString)
    val v = for (name <- nm) yield {
      for (savedB <- Brewery(name, none).persist _) yield savedB
    }

    val r: NodeSeq = v map (Database.run(_): Brewery) map {
      v => <p>Saved {v.name}.</p>
    } getOrElse {
      <p>No name.</p>
    }

    render(r)(request)
  }
}

class BeersController extends BaseController {
  override def nnew(request: Request[Stream]) = Some {
    val v = for (breweries <- Brewery.all _) yield render(partials.newBeerForm(breweries))(request)
    Database.run(v)
  }
  
  override def create(request: Request[Stream]) = Some { render {
      val name = (request | "name") map (_.mkString)
      val breweryKey = (request | "brewery") map (_.mkString)
      <div>
        <p>
          {name}
        </p>
        <p></p>
      </div>
    }(request)
  }
}

object Home {
  def userService = UserServiceFactory.getUserService
  
  def admin(request: Request[Stream]): Option[Request[Stream]] = userService.currentUser >| request
  
  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)
    
  val login = ((r: Request[Stream]) => redirectTo(userService.createLoginURL("/"))(r)).kleisli[Option]
  
  def route = {
    val start = new StartController
    val breweries = new BreweriesController
    val beers = new BeersController
    
    val app = check(☆(admin _), login) { List(
      at(Nil) >=> m(GET) >=> (start.index _),
      beers.mountByName,
      breweries.mountByName
    ).∑ }
    app
  }
}

