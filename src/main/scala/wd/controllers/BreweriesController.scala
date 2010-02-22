package wd
package controllers

import scalaz._
import Scalaz._

import wd._
import scapps._
import gae._
import rest._
import belt._
import com.google.appengine.api.datastore._
import wd.views.breweries
import sage._

object BreweriesController extends RestController[Keyed[Brewery]] {
  import scapps.Global._
  import Services._
  
  def ds = datastoreService
  
  def apply(v: Action[Keyed[Brewery]]): Option[Response] = v match {
    case New => render(breweries.nu(request.formBase(Nil))) η

    case rest.Show(brewery) => {
      val beers = Beers.childrenOf(brewery.key)
      render(breweries.show(brewery, beers))
    } η

    case Edit(brewery) => { render(breweries.edit(brewery, Nil)) } η

    case Create => {
      
      val readB = request.create[Brewery]
      val saved = readB ∘ (Breweries << _)
      
      saved fold ({errors => 
        render(breweries.nu(request.formBase(errors.list)))
      }, _ => belt.redirect("/"))
    } η

    case Update(brewery) => {
        val (errors, updated) = request.update(brewery.value)
        val newKeyed = Keyed(brewery.key, updated)
        errors match {
          case Nil => rest.redirect(Breweries << newKeyed)
          case (_ :: _) => render(breweries.edit(newKeyed, errors))
        }
    } η

    case Destroy(brewery) => None
    case rest.Index => None
  }
}
