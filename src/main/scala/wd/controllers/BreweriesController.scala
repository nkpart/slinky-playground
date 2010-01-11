package wd
package controllers

import scalaz._
import Scalaz._
import scalaz.http.request.Request
import wd.{Database, Brewery}
import wd.views.breweries

class BreweriesController(implicit val request: Request[Stream]) extends BaseController {
  import Database.runDb
  override def show(keyName: String) = Some {
    val x = (Brewery.findByKeyName(keyName) _) >>= (bm => ds => {
      or404(bm map { br =>
        val beers = br.beers(ds)
        render(breweries.show(br, beers))
      })
    })
    runDb(x)
  }

  override def edit(keyName: String) = Some {
    val v = (Brewery.findByKeyName(keyName) _) ∘∘ { (brewery: Brewery) =>
      render(breweries.edit(brewery))
    }
    runDb(v ∘ { or404(_) })
  }

  override def create = Some { runDb { ds =>
    val readB = request.read[Brewery]
    val saved: Posted[Brewery] = readB ∘ { brewery => brewery.persist(ds) }

    ((saved >| { redirectTo("/") }).fail ∘ { (errors: List[NamedError]) =>
      render(<p>No name.</p>)
    }).validation.fold(identity, identity)
  }}

  override def update(id: String) = Some {
    val v = request("name") ∘ (name => Brewery(name, none).persist _)
    v map (runDb(_): Brewery) map {
      v => redirectTo("/")
    } getOrElse {
      render(<p>No name.</p>)
    }
  }

  override def nnew = Some {
    render(breweries.nnew)
  }
}
