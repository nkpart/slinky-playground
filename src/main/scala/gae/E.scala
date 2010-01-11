package gae

import com.google.appengine.api.datastore._

trait Kind[T] { val kind: String   }

trait E[T] {
  def withKey(key: Key): T

  val key: Option[Key]

  // Key creation options
  val parent: Option[Key] = None
  def keyName: Option[String] = None

  def writeToEntity(entity: Entity): Unit

  def baseEntity(implicit k: Kind[T]): Entity = {
    lazy val unsavedEntity = (parent, keyName) match {
      case (Some(pk), Some(n)) => new Entity(k.kind, n, pk)
      case (Some(pk), None) => new Entity(k.kind, pk)
      case (None, Some(n)) => new Entity(k.kind, n)
      case (None, None) => new Entity(k.kind)
    }
    key map (k => new Entity(k)) getOrElse unsavedEntity
  }

  def asEntity(implicit k: Kind[T]) = {
    val e = baseEntity
    writeToEntity(e)
    e
  }

  def persist(ds: DatastoreService)(implicit k: Kind[T]): T = {
    val key = ds.put(asEntity)
    this withKey key
  }
}
