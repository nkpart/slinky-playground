package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

trait DatastoreServiceW {
  val ds: DatastoreService

  def all[T](implicit k : Kind[T], ec: EntityCreatable[T]) : Iterable[Keyed[T]] =
    Find[T].iterable(ds)
  
  def findById[T](id: Long)(implicit k: Kind[T], ec: EntityCreatable[T]): Option[Keyed[T]] = {
    val key = createKey[T](id)
    (() => ds.get(key)).throws.success >>= (entityTo(_).create[T])
  }
}

trait DatastoreServiceImplicits {
  implicit def datastoreTo[T](d: DatastoreService): DatastoreServiceW = new DatastoreServiceW { val ds = d }
  implicit def datastoreFrom[T](t: DatastoreServiceW): DatastoreService = t.ds
}
