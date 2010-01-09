package wd.controllers

import scalaz.http.request.Request
import wd.{Database, Brewery}
import wd.views.breweries

class BreweriesController(implicit val request: Request[Stream]) extends BaseController {
  override def show = Some {
    val keyName = request("id").get
    val v: Option[Brewery] = Database.runDb(Brewery.findByKeyName(keyName) _)
    val r = v ∘ { brewery =>
      val beers = Database.runDb(brewery.beers _)
      render(breweries.show(brewery, beers))
    }
    or404(r)
  }

  override def edit = Some {
    val keyName = request("id").get
    val v = (Brewery.findByKeyName(keyName) _) ∘∘ { (brewery: Brewery) =>
      render(breweries.edit(brewery))
    }
    Database.runDb(v ∘ { or404(_) })
  }

  override def create = Some {
    val v = request("name") ∘ (name => Brewery(name, none).persist _)
    v map (Database.runDb(_): Brewery) map {
      v => redirectTo("/")
    } getOrElse {
      render(<p>No name.</p>)
    }
  }

  override def update = Some {
    val id = request("id").get
    val v = request("name") ∘ (name => Brewery(name, none).persist _)
    v map (Database.runDb(_): Brewery) map {
      v => redirectTo("/")
    } getOrElse {
      render(<p>No name.</p>)
    }
  }

  override def nnew = Some {
    render(breweries.nnew)
  }
}
