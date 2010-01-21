package wd
package controllers

import scalaz._
import http.response.Response
import Scalaz._
import wd.views.beers
import scalaz.http.request.Request
import wd._
import scapps._
import rest._
import gae._
import wd.Beer._
import com.google.appengine.api.datastore.DatastoreService

class BeersController(val ds: DatastoreService)(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  
  def handle(v: Action[String]) = v match {
    case New => Some {
      val breweryId = request("breweryKey")
      breweryId ∘ { id =>
        ~(Brewery.findById(id)(ds) ∘ { brewery =>
          render(beers.nnew(Left(brewery)))
        })
      } getOrElse {
        render(beers.nnew(Right(Brewery.all(ds))))
      }
    }

    case Create => Some {
      val c = request.create[Beer].either.right.toOption
      val breweryKey = request("brewery").get
      val br = Brewery.findById(breweryKey)(ds)
      val persisted = (c <×> br) map { case b@(beer, _) => beer.persistWithKey(b)(ds) }
      render {
        <div>
          <p>
            {c}
          </p>
        </div>
      }
    }

    case _ => none
  }
}
