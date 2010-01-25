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
      val breweryId = request("brewery_id") map (_.toLong)
      breweryId ∘ { id =>
        ~(ds.findById[Brewery](id) ∘ { brewery =>
          render(beers.nu(Left(brewery)))
        })
      } getOrElse {
        val all: Iterable[Keyed[Brewery]] = Brewery.allByName(ds)
        render(beers.nu(Right(all)))
      }
    }

    case Create => Some {
      val c: Option[Beer] = request.create[Beer].success
      val breweryKey = request("brewery_id") map (_.toLong) get
      val br = ds.findById[Brewery](breweryKey)
      val persisted = (c <×> br) map { case (beer, brk) => beer.insertWithParent(brk.key, ds) }
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
