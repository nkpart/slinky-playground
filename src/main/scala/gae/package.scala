import gae._

import scalaz._
import Scalaz._

trait GAEBase extends 
  IdentityImplicits with
  DatastoreServiceImplicits with
  EntityImplicits with
  UserServiceImplicits
  
package object gae extends GAEBase {

  import com.google.appengine.api.datastore._
  def createQuery[T](implicit s: Kind[T]) = new Query(s.kind)
  def createKey[T](name: String)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, name)
  def createKey[T](id: Long)(implicit s : Kind[T]) = KeyFactory.createKey(s.kind, id)
}


