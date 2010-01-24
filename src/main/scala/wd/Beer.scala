package wd

import scalaz._
import http.request.Request
import Scalaz._

import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.{DatastoreService, Key, Entity}
import gae._
import scapps.{RequestUpdate, RequestCreate}

case class Beer(name: String) {
  def writeToEntity(e: Entity) {
    e.setProperty("name", name)
  }
}

object Beer {
  def fromEntity(e: Entity): Option[Keyed[Beer]] = {
    val name = Option(e.getProperty("name").asInstanceOf[String])
    name map (n => Keyed(Beer(n), e.getKey))
  }

  def all(ds: DatastoreService): Iterable[Keyed[Beer]] = {
    val es: Iterable[Entity] = ds.prepare(createQuery[Beer]).asIterable
    val breweries: Key => Entity = ds.get(es map (_.getParent))
    es flatMap fromEntity _
  }

  val Required = "%s is required."

  def required[IN[_] : FoldLeft](r: Request[IN])(s: String)(fmt: String) = r(s).toSuccess(s -> fmt.format(s))
}