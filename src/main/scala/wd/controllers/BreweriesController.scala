package wd
package controllers

import scalaz._
import http.response.Response
import Scalaz._
import scalaz.http.request.Request
import wd.{Database, Brewery}
import wd.views.breweries
import scapps._
import gae._
import rest._
import com.google.appengine.api.datastore.DatastoreService

class BreweriesController(val ds: DatastoreService)(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  import Database._

  def get2[T](f: String => Option[T])(key: String)(g: T => Response[Stream]): Response[Stream] = ~(f(key) ∘ g)

  def getOr404[T](keyName: String)(f: (Brewery => Response[Stream]))(ds: DatastoreService) = get2 {Brewery.findByKeyName(_)(ds)}(keyName)(f)

  def find(keyName: String) = Brewery.findByKeyName(keyName)(ds)

  def handle(v: Action): Option[Response[Stream]] = v match {
    case New => render(breweries.nnew) η

    case rest.Show(keyName) => {
      find(keyName) ∘ {
        br =>
          render(breweries.show(br, br.beers(ds)))
      }
    }

    case Edit(keyName) => {
      find(keyName) ∘ {
        (brewery: Brewery) =>
          render(breweries.edit(brewery))
      }
    }

    case Create => {
      val readB = request.create[Brewery]
      val saved: Posted[Brewery] = readB ∘ {brewery => {persist(brewery)(ds)}}
      ((saved >| {redirectTo("/")}).fail ∘ {
        (errors: List[NamedError]) =>
          render(<p>No name.</p>)
      }).validation.fold(identity, identity)
    } η

    case Update(keyName) => find(keyName) ∘ { (br: Brewery) =>
        val (errors, updated): (List[FieldError], Brewery) = request.update(br)
        errors match {
          case Nil => redirectTo(persist(updated)(ds))
          case (_ :: _) => render(breweries.edit(updated, errors))
        }
    }


    case Destroy(id) => None
    case rest.Index => None
  }
}
