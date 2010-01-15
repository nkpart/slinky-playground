package wd
package controllers

import scalaz._
import http.response.Response
import Scalaz._
import scalaz.http.request.Request
import wd.{Database, Brewery}
import wd.views.breweries
import scapps._
import rest._
import com.google.appengine.api.datastore.DatastoreService

class BreweriesController(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  import Database._

  def get2[T](f: String => Option[T])(key: String)(g: T => Response[Stream]): Response[Stream] = {
    ~(f(key) ∘ g)  
  }

  def getOr404[T](keyName: String)(f: (Brewery => Response[Stream]))(ds: DatastoreService) = {
    get2 { Brewery.findByKeyName(_)(ds) } (keyName)(f)
  }

  def handle(v: Verb): Option[Response[Stream]] = v match {
    case New => render(breweries.nnew) η

    case scapps.Show(keyName) => Some {
      runDb(ds => {
        getOr404(keyName) { br =>
           render(breweries.show(br, br.beers(ds)))
        }(ds)
      })
    }

    case Edit(keyName) => Some {
      runDb(ds => {
        getOr404(keyName) { (brewery: Brewery) =>
            render(breweries.edit(brewery))
        }(ds)
      })
    }

    case Create => Some {
      runDb {
        ds => {
          val readB = request.read[Brewery]
          val saved: Posted[Brewery] = readB ∘ {brewery => brewery.persist(ds)}
          ((saved >| {redirectTo("/")}).fail ∘ {
            (errors: List[NamedError]) =>
              render(<p>No name.</p>)
          }).validation.fold(identity, identity)
        }
      }
    }

    case Update(keyName) => Some { runDb { ds =>
      val v = Brewery.findByKeyName(keyName)(ds) ∘ {
        (br: Brewery) =>
          val (errors, updated) = request.update(br)
          errors match {
            case Nil => {
              val persisted = updated.persist(ds)
              redirectTo(persisted.rr.show)
            }
            case (_ :: _) => {
              render(breweries.edit(updated))
            }
          }
      }
      ~(v)
    }}
    case Destroy(id) => None
    case scapps.Index => None
  }
}
