package wd

import scalaz._
import Scalaz._

import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.{DatastoreService, Key, Entity}
import gae._

case class Beer(name: String, brewery: Brewery, key: Option[Key]) extends E[Beer] {
  override val parent = brewery.key

  def withKey(k: Key) = this.copy(key = Some(k))

  def writeToEntity(e: Entity) {
    e.setProperty("name", name)
  }
}

object Beer {
  def fromEntity(e: Entity, brewery: Brewery): Option[Beer] = {
    val name = Option(e.getProperty("name").asInstanceOf[String])
    name map (Beer(_, brewery, Option(e.getKey)))
  }

  def all(ds : DatastoreService) : Iterable[Beer] = {
    val es: Iterable[Entity] = ds.prepare(createQuery[Beer]).asIterable
    val breweries: Key => Entity = ds.get(es map (_.getParent))
    val lookupBrewery = breweries map (Brewery.fromEntity _) map (_.get)
    es flatMap { e => fromEntity(e, lookupBrewery(e.getParent)) }
  }
}