package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

trait Model[T] extends EntityBase[T] with EntityWriteable[T] with EntityCreatable[T] {
  def entityBase: EntityBase[T]
  def entityWriteable: EntityWriteable[T]
  def entityCreatable: EntityCreatable[T]
  
  def kind = entityBase.kind
  def keyName(t: T) = entityBase.keyName(t)
  def write(t: T, e: Entity) = entityWriteable.write(t,e)
  def createFrom(e: Entity) = entityCreatable.createFrom(e)
}

trait Kind[T] {
  def kind: String
}

trait EntityBase[T] extends Kind[T] {
  def keyName(t: T): Option[String]

  def createBase(t: T, parentKey: Option[Key]): Entity = {
    (keyName(t), parentKey) match {
      case (Some(n), Some(pk)) => new Entity(kind, n, pk)
      case (Some(n), None) => new Entity(kind, n)
      case (None, Some(pk)) => new Entity(kind, pk)
      case (None, None) => new Entity(kind)
    }
  }
}

trait EntityWriteable[T] { def write(t: T, e: Entity) }
trait EntityCreatable[T] { def createFrom(e: Entity): Option[T] }
