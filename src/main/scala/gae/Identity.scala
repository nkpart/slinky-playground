package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

trait Identity[T] {
  val value: T

  def insert(ds: DatastoreService)(implicit ew: EntityWriteable[T], stored: EntityBase[T]): Keyed[T] = {
    val newKey = ds.put(entity(value,value, None))
    Keyed(value, newKey)
  }

  def insertWithParent(parentKey: Key, ds: DatastoreService)(implicit ew: EntityWriteable[T], stored: EntityBase[T]): Keyed[T] = {
    val newKey = ds.put(entity(value,value, Some(parentKey)))
    Keyed(value, newKey)
  }

  private def entity[KT, VT](kt: KT, vt: VT, parentKey: Option[Key])(implicit base: EntityBase[KT], ew: EntityWriteable[VT])= {
    val e = base.createBase(kt, parentKey)
    ew.write(vt, e)
    e
  }
}

trait IdentityImplicits {
  implicit def identityTo[T](t: T) = new gae.Identity[T] { val value = t }
  implicit def identityFrom[T](t: gae.Identity[T]): T = t.value  
}
