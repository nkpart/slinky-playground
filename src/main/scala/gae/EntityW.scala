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
