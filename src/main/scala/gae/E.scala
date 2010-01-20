package gae

import com.google.appengine.api.datastore._
import wd.Brewery

trait Stored[T] {
  val kind: String
  def keyName(t: T): String
  def withKey(t: T, k: Key): T
}

trait EntityWriteable[T] { def write(t: T, e: Entity) }

trait EntityCreatable[T] { def create(e: Entity): T }

