package wd
package controllers

import scalaz._
import Scalaz._
import wd.views.beers
import scalaz.http.request.Request
import wd.{Brewery, Database}

class BeersController(implicit val request: Request[Stream]) extends BaseController {
  override def nnew = Some {
    val v = (Brewery.all _) ∘ (bs => render(beers.nnew(bs)))
    Database.runDb(v)
  }

  override def create = Some {
    val name = request("name")
    val breweryKey = request("brewery")
    render {
      <div>
        <p>{name}</p>
      </div>
    }
  }
}
