package wd
package controllers

import scalaz._
import Scalaz._
import com.google.appengine.api.users._
import com.google.appengine.api.datastore._
import scalaz.http.request._

import wd.views._

object Start extends BaseController {
  import scapps.R._
  import gae.Services._
  
  def ds = datastoreService
  
  def root = {
    val breweries =  Brewery.allByName(ds)
    render(start.index(breweries))
  }
  
  def config = {
    val styles = List(Style("a"))
    render(start.config(styles))
  }
}
