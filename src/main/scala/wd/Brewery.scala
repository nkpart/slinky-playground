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
  implicit def lookupBeers(kb: Keyed[Brewery]) = new {
    def beers(ds: DatastoreService): Iterable[Keyed[Beer]] = {
      query[Beer](ds) { qry =>
        qry.setAncestor(kb.key)
      } flatMap (Beer.fromEntity _)
    }
  }
  
  def all(ds : DatastoreService) = Queries.all[Brewery](_.create[Brewery], ds)
  
  def allByName(ds : DatastoreService): Iterable[Keyed[Brewery]] = {
    val qry = createQuery[Brewery].addSort("name", SortDirection.ASCENDING)
    ds.prepare(qry).asIterable flatMap(_.create[Brewery])
  }
}
