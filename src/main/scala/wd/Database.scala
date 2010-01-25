package wd

import scalaz.Scalaz._
import com.google.appengine.api.datastore._
import gae._
import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.Query.SortDirection
import scalaz.http.request.Request

object Database {
  type DB[T] = (DatastoreService => T)
  private def datastore = DatastoreServiceFactory.getDatastoreService
  
  def runDb[T](f : DB[T]) : T = f(datastore)

  def liftDb[T](t : => T): DB[T] = (ds: DatastoreService) => { t }
}