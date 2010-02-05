package wd
package controllers

import sage._
import scalaz._
import http.response.Response
import Scalaz._
import wd.views.beers
import scalaz.http.request.Request
import wd._
import scapps._
import rest._
import gae._
import com.google.appengine.api.datastore.DatastoreService

object BeersController extends RestController[String] {
  import scapps.R._
  import Services._
  
  def ds = datastoreService
  
  def apply(v: Action[String]) = v match {
    case New => {
      // TODO Add contexts matching in.
      val breweryId = request("brewery_id") map (_.toLong)
      breweryId some { id =>
        Breweries.lookup(id) ∘ { brewery =>
          render(beers.nu(Left(brewery)))
        }
      } none {
        val breweries = Breweries.allByName    
        Some(render(beers.nu(Right(breweries))))
      }
    }

    case Create => Some {
      val readB = request.create[Beer]
      val breweryKey = request("brewery_id") map (_.toLong) get
      val br = Breweries.lookup(breweryKey).toSuccess(("brewery" -> "Unknown brewery").wrapNel)
      val persisted = (readB <|*|> br) map { case (beer, brk) => {
        val inserted = Beers.parentedSave(beer, parent = brk.key)
        request.redirectTo(brk)
      }}

      persisted fold ({errors =>
        val all = Breweries.allByName
        render(beers.nu(Right(all)))
      }, identity)
    }

    case _ => none
  }
}
