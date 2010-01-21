package wd
package controllers

import scalaz._
import Scalaz._
import scalaz.http.response.Response
import scalaz.http.request.Request

import wd.views.breweries
import wd._
import scapps._
import gae._
import rest._
import com.google.appengine.api.datastore._

class BreweriesController(val ds: DatastoreService)(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  import Database._

  def get2[T](f: String => Option[T])(key: String)(g: T => Response[Stream]): Response[Stream] = ~(f(key) ∘ g)
  def getOr404[T](keyName: String)(f: (Brewery => Response[Stream]))(ds: DatastoreService) = get2 {Brewery.findById(_)(ds)}(keyName)(f)

  def find(keyName: String): Option[Brewery] = Brewery.findById(keyName)(ds)
  def lookup(v: Action[String]): Option[Action[Brewery]] = v ↦ (find _) // Holy shit that was rad.
  def handle(v: Action[String]): Option[Response[Stream]] = lookup(v) >>= (handleB _)
  
  def handleB(v: Action[Brewery]): Option[Response[Stream]] = v match {
    case New => render(breweries.nnew) η

    case rest.Show(brewery) => {
      val beers = brewery.beers(ds)
      render(breweries.show(brewery, beers))
    } η

    case Edit(brewery) => {
      render(breweries.edit(brewery, Nil))
    } η

    case Create => {
      val readB = request.create[Brewery]
      val saved: Posted[Brewery] = readB ∘ {brewery => {brewery.persist(ds)}}
      ((saved >| {redirectTo("/")}).fail ∘ {
        (errors: List[NamedError]) =>
          render(<p>No name.</p>)
      }).validation.fold(identity, identity)
    } η

    case Update(brewery) => {
        val (errors, updated) = request.update(brewery)
        errors match {
          case Nil => redirectTo(updated.persist(ds))
          case (_ :: _) => render(breweries.edit(updated, errors))
        }
    } η

    case Destroy(brewery) => None
    case rest.Index => None
  }
}
