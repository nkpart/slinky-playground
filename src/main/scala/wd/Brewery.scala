package wd

import gae.E
import com.google.appengine.api.datastore.{DatastoreService, Entity, Key}
import com.google.appengine.api.datastore.Query.SortDirection
import scalaz.http.request.Request
import gae.Kind._
import scapps.Postable
import scalaz._
import Scalaz._

case class Brewery(name: String, key: Option[Key]) extends E[Brewery] {
  override def keyName = Some(Brewery.keyName(name))
  def withKey(k: Key) = this.copy(key = Some(k))

  def writeToEntity(e: Entity) {
    e.setProperty("name", name)
  }

  def beers(datastore: DatastoreService): Iterable[Beer] = {
    // TODO should key be split off from the class? should only be able to
    // get beers for a saved brewery
    val query = createQuery[Beer].setAncestor(key.get)
    datastore.prepare(query).asIterable() flatMap (Beer.fromEntity(_, this))
  }
}

object Brewery {
  def keyName(name: String) = name.split(" ").reduceLeft(_ + "-" + _)
  def all(ds : DatastoreService) = Queries.all[Brewery](fromEntity _, ds)
  def allByName(ds : DatastoreService): Iterable[Brewery] = {
    val qry = createQuery[Brewery].addSort("name", SortDirection.ASCENDING)
    ds.prepare(qry).asIterable flatMap(fromEntity _)
  }

  def findByKeyName(name: String)(ds: DatastoreService): Option[Brewery] = {
    val key = createKey[Brewery](name)
    Option(ds.get(key)) >>= fromEntity _
  }

  def fromEntity(e: Entity): Option[Brewery] = {
    val name = Option(e.getProperty("name").asInstanceOf[String])
    name map (Brewery(_, Option(e.getKey)))
  }
  
  implicit object postable extends Postable[Brewery] {
    val Required = "%s is required."
    
    def required[IN[_]](r: Request[IN])(s : String)(fmt: String) = r(s).toSuccess(s -> fmt.format(s))

    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ { n => Brewery(n, none) }
    }

    def update[IN[_]: FoldLeft](r: Request[IN])(brewery: Brewery) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ { n => brewery copy (name = n) } fold (err => (err, brewery), br => (Nil, br))
    }
  }
}
