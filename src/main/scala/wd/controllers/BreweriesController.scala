package wd
package controllers

import scalaz._
import Scalaz._

import wd._
import scapps._
import gae._
import rest._

import scalaz.http.response.Response
import scalaz.http.request.Request
import com.google.appengine.api.datastore._
import wd.views.breweries

class BreweriesController(val ds: DatastoreService)(implicit val request: Request[Stream]) extends Controller with ControllerHelpers {
  
  def find(keyName: String): Option[Brewery] = Brewery.findById(keyName)(ds)
  
  def handle(v: Action[String]): Option[Response[Stream]] = v ↦ (find _) >>= (handleB _)
  
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