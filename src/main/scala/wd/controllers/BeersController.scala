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
import wd.Beer._
import com.google.appengine.api.datastore.DatastoreService

class BeersController(val ds: DatastoreService)(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  def handle(v: Action) = v match {
    case New => Some {
      val breweryKey = request("breweryKey")
      breweryKey ∘ { key =>
        ~(Brewery.findByKeyName(key)(ds) ∘ { brewery =>
          render(beers.nnew(Left(brewery)))
        })
      } getOrElse {
        render(beers.nnew(Right(Brewery.all(ds))))
      }
    }

    case Create => Some {
      val c = request.create[Brewery => Beer]
      val breweryKey = request("brewery")
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
