package wd
package controllers

import sage._
import scalaz._
import scalaz.http.response.Forbidden
import Scalaz._
import scalaz.http.request.Request
import wd._
import scapps._
import rest._
import gae._
import com.google.appengine.api.datastore.DatastoreService
import belt.Response
import views.{errors, beers}

object BeersController extends RestController[String] {
  import scapps.Global._
  import Services._

  def nu: Option[Response] = {
    val breweryId = request("brewery_id") >>= (_.parseLong.success)
    breweryId some { id =>
      Breweries.lookup(id) ∘ { brewery =>
        render(beers.nu(Left(brewery)))
      }
    } none {
      val breweries = Breweries.allByName
      Some(render(beers.nu(Right(breweries))))
    }
  }

  def create: Response = {
    val readB = request.create[Beer]
    val breweryKey = request("brewery_id") >>= (_.parseLong.success)  
    val br = (breweryKey >>= Breweries.lookup _).toSuccess(("brewery" -> "Unknown brewery").wrapNel)
    val persisted = (readB <|*|> br) map { case (beer, brk) => {
      val inserted = Beers.parentedSave(beer, parent = brk.key)
      redirect(brk)
    }}

    persisted ||| { errors =>
      val all = Breweries.allByName
      render(beers.nu(Right(all)))
    }
  }

  // TODO Add contexts matching in.
  def apply(v: Action[String]) = v match {
    case New => (adminOnly { nu }.fail ∘ (some(_))).validation.either.merge
    case Create => adminOnly { create }.either.merge η 
    case _ => none
  }
}
