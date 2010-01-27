package wd

import com.google.appengine.api.datastore.{DatastoreService, Entity, Key}
import com.google.appengine.api.datastore.Query.SortDirection
import scalaz.http.request.Request
import gae._
import scalaz._
import Scalaz._
import scapps.{RequestCreate, RequestUpdate}

// Location
// * full address.
// isPub

case class Country(value: String)
case class Brewery(name: String, country: Country)

object Brewery {
  def allByName(ds : DatastoreService): Iterable[Keyed[Brewery]] = {
    ds.query[Brewery] { _.addSort("name", SortDirection.ASCENDING) }
  }
}
