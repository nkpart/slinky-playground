package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

trait Keyed[T] {
  val key: Key
  val value: T

  def save(ds: DatastoreService)(implicit ew: EntityWriteable[T]): Keyed[T] = {
    val e = new Entity(key)
    ew.write(value, e)
    Keyed(value, ds.put(e))
  }
  
  // TODO: move this onto Query
  def children[T](ds: DatastoreService)(implicit ec: EntityCreatable[T], k: Kind[T]): Iterable[Keyed[T]] = {
    Find[T].query(_.setAncestor(this.key)).iterable(ds)
  }
}

object Keyed {
  def apply[T](t: T, k: Key) = new Keyed[T] { val value = t; val key = k}
}
