import gae._

import scalaz._
import Scalaz._

package object gae extends 
  IdentityImplicits with
  DatastoreServiceImplicits with
  EntityImplicits with
  UserServiceImplicits 
  {

  import com.google.appengine.api.datastore._
  def createQuery[T](implicit s: Kind[T]) = new Query(s.kind)
  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


