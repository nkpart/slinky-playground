package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

trait EntityW {
  val entity: Entity

  def create[T](implicit ec: EntityCreatable[T]): Option[Keyed[T]] = {
    ec.createFrom(entity) map { t => Keyed(t, entity.getKey) }
  }
}

trait EntityImplicits {
  implicit def entityTo(en: Entity) = new EntityW { val entity = en }
  implicit def entityFrom(re: EntityW) = re.entity
}

// TODO figure out a better name for these traits

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
