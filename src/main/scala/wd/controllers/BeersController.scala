package wd
package controllers

import scalaz._
import http.response.Response
import Scalaz._
import wd.views.beers
import scalaz.http.request.Request
import wd.{Brewery, Database}
import scapps._

class BeersController(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  def handle(v: Verb) = v match {
    case New => Some {
      val v = (Brewery.all _) ∘ (bs => render(beers.nnew(bs)))
      Database.runDb(v)
    }

    case Create => Some {
      val name = request("name")
      val breweryKey = request("brewery")
      render {
        <div>
          <p>
            {name}
          </p>
        </div>
      }
    }

    case _ => none
  }
}
