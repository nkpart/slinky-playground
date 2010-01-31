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

object BeersController extends Controller with ControllerHelpers {
  import scapps.R._
  import Services._
  
  def ds = datastoreService
  
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
      val readB = request.create[Beer]
      val breweryKey = request("brewery_id") map (_.toLong) get
      val br = ds.findById[Brewery](breweryKey).toSuccess(("brewery" -> "Unknown brewery").wrapNel)
      val persisted = (readB <|*|> br) map { case (beer, brk) => {
        val inserted = beer.insertWithParent(brk.key, ds)
        request.redirectTo(brk)
      }}

      persisted fold ({errors => 
        val all: Iterable[Keyed[Brewery]] = Brewery.allByName(ds)
        render(beers.nu(Right(all)))
      }, identity)
    }

    case _ => none
  }
}
