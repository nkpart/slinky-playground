package wd

import scalaz._
import http.request.Request
import Scalaz._

import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.{DatastoreService, Key, Entity}
import gae._
import scapps.{RequestUpdate, RequestCreate}

case class Beer(name: String, brewery: Brewery, key: Option[Key]) {
  def writeToEntity(e: Entity) {
    e.setProperty("name", name)
  }
}

object Beer {
  def fromEntity(e: Entity, brewery: Brewery): Option[Beer] = {
    val name = Option(e.getProperty("name").asInstanceOf[String])
    name map (Beer(_, brewery, Option(e.getKey)))
  }

  def all(ds: DatastoreService): Iterable[Beer] = {
    val es: Iterable[Entity] = ds.prepare(createQuery[Beer]).asIterable
    val breweries: Key => Entity = ds.get(es map (_.getParent))
    val lookupBrewery = breweries map (Brewery.fromEntity _) map (_.get)
    es flatMap {e => fromEntity(e, lookupBrewery(e.getParent))}
  }

  val Required = "%s is required."

  def required[IN[_] : FoldLeft](r: Request[IN])(s: String)(fmt: String) = r(s).toSuccess(s -> fmt.format(s))

  implicit object requestCreate extends RequestCreate[(Brewery => Beer)] {
    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ {n => Beer(n, _, none)}
    }
  }

  implicit object requestUpdate extends RequestUpdate[Beer] {
    def update[IN[_] : FoldLeft](r: Request[IN])(beer: Beer) = {
      val name = required(r)("name")(Required).fail.lift[List, (String, String)]
      name ∘ {n => beer copy (name = n)} fold (err => (err, beer), br => (Nil, br))
    }
  }

}