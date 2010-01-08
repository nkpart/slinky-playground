package wd

import scalaz.Scalaz._
import com.google.appengine.api.datastore._
import gae._
import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.Query.SortDirection

import Kind._

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

object Queries {
  def all[T](f : Entity => Traversable[T], datastore: DatastoreService)(implicit k : Kind[T]) : Iterable[T] = {
    val es: Iterable[Entity] = datastore.prepare(createQuery[T]).asIterable
    es flatMap f
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
}

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

object Database {
  private def datastore = DatastoreServiceFactory.getDatastoreService
  
  def runDb[T](f : DatastoreService => T) : T = f(datastore)
}