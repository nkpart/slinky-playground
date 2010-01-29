import com.google.appengine.api.datastore._
import com.google.appengine.api.users.{UserService, UserServiceFactory, User}
import gae._

import scalaz._
import Scalaz._

package gae {
  trait Kind[T] {
    def kind: String
  }

  // Entities need to be created 
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
}

package object gae extends 
  IdentityImplicits with
  DatastoreServiceImplicits with
  EntityImplicits with
  UserServiceImplicits 
  {

  def createQuery[T](implicit s: Kind[T]) = new Query(s.kind)
  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


