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
import wd.Brewery._

object Breweries extends RestController[Keyed[Brewery]] {
  import scapps.R._
  import Services._
  
  def ds = datastoreService
  
  def apply(v: Action[Keyed[Brewery]]): Option[Response[Stream]] = v match {
    case New => render(breweries.nu(request.formBase(Nil))) η

    case rest.Show(brewery) => {
      val beers = brewery.children[Beer](ds)
      render(breweries.show(brewery, beers))
    } η

    case Edit(brewery) => { render(breweries.edit(brewery, Nil)) } η

    case Create => {
      val readB = request.create[Brewery]
      val saved = readB ∘ (_.insert(ds))
      
      saved fold ({errors => 
        render(breweries.nu(request.formBase(errors.list)))
      }, _ => request.redirectTo("/"))
    } η

    case Update(brewery) => {
        val (errors, updated) = request.update(brewery.value)
        val newKeyed = Keyed(updated, brewery.key)
        errors match {
          case Nil => request.redirectTo(newKeyed.save(ds))
          case (_ :: _) => render(breweries.edit(newKeyed, errors))
        }
    } η

    case Destroy(brewery) => None
    case rest.Index => None
  }
}
