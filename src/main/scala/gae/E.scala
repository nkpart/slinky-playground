package gae

import com.google.appengine.api.datastore.{DatastoreService, Entity, Key}

trait Kind[T] { val kind: String   }

trait E[T] {
  def withKey(key: Key): T

  val key: Option[Key]
  val parent: Option[Key] = None

  def writeToEntity(entity: Entity): Unit

  def baseEntity(implicit k: Kind[T]): Entity = {
    lazy val unsavedEntity = parent map (new Entity(k.kind, _)) getOrElse (new Entity(k.kind))
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
