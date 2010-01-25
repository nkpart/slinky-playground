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
// * country
// isPub

case class Brewery(name: String)//, country: Country, isPub: Boolean)

object Brewery {
  def allByName(ds : DatastoreService): Iterable[Keyed[Brewery]] = {
    query[Brewery](ds) { _.addSort("name", SortDirection.ASCENDING) }
  }
}
