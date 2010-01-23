package gae

import com.google.appengine.api.datastore._
import wd.Brewery

case class Keyed[T](value: T,key: Key)

trait Kind[T] {
  def kind: String
}

// Something that can be stored.
trait KeyFor[T] extends Kind[T] {
  def keyFor(t: T): Option[Key]
  def keyName(t: T): Option[String]
  def parentKey(t: T): Option[Key]
  
  def createEntity(t: T): Entity = {
    (keyName(t), parentKey(t)) match {
      case (Some(n), Some(pk)) => new Entity(kind, n, pk)
      case (Some(n), None) => new Entity(kind, n)
      case (None, Some(pk)) => new Entity(kind, pk)
      case (None, None) => new Entity(kind)
    }
  }
  
  def entity(t: T): Entity = {
    keyFor(t) map { k =>
      new Entity(k)
    } getOrElse { createEntity(t) }
  }
  
  def withKey(t: T, k: Key): T
}

trait EntityWriteable[T] { def write(t: T, e: Entity) }

trait EntityCreatable[T] { def create(e: Entity): T }

