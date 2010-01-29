package wd

import com.google.appengine.api.datastore._
import com.google.appengine.api.datastore.Query.SortDirection
import gae._
import scalaz._
import Scalaz._

// Location
// * full address.
// isPub

case class Country(value: String)
case class Brewery(name: String, country: Country)

object Brewery {
  val allByName = Find[Brewery].query(_.addSort("name", SortDirection.ASCENDING)).iterable _
}
