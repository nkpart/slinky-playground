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

case class Brewery(name: String, key: Option[Key]) {
  def beers(datastore: DatastoreService): Iterable[Beer] = {
    // TODO should key be split off from the class? should only be able to
    // get beers for a saved brewery
    val query = createQuery[Beer].setAncestor(key.get)
    datastore.prepare(query).asIterable() flatMap (Beer.fromEntity _)
  }
}

object Brewery {
  def all(ds : DatastoreService) = Queries.all[Brewery](fromEntity _, ds)
  def allByName(ds : DatastoreService): Iterable[Brewery] = {
    val qry = createQuery[Brewery].addSort("name", SortDirection.ASCENDING)
    ds.prepare(qry).asIterable flatMap(fromEntity _)
  }

  def findById(id: String)(ds: DatastoreService): Option[Brewery] = {
    val key = createKey[Brewery](id.parseLong.success.get)
    Option(ds.get(key)) >>= fromEntity _
  }

  def fromEntity(e: Entity): Option[Brewery] = {
    val name = Option(e.getProperty("name").asInstanceOf[String])
    name map (Brewery(_, Option(e.getKey)))
  }
  
  implicit object postable extends RequestCreate[Brewery] with RequestUpdate[Brewery] {
    val Required = "%s is required."
    
    def required[IN[_]: FoldLeft](r: Request[IN])(s : String)(fmt: String) = r(s).toSuccess(s -> fmt.format(s))

    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ { n => Brewery(n, none) }
    }

    def update[IN[_]: FoldLeft](r: Request[IN])(brewery: Brewery) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ { n => brewery copy (name = n) } fold ((_, brewery), (Nil, _))
    }
  }
}
