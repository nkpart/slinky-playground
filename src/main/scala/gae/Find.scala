package gae

import com.google.appengine.api.datastore._
import scalaz._
import Scalaz._

case class Find[T](q: Query, fo: FetchOptions) {
  def query(f: Query => Query): Find[T] = Find(f(q), fo)

  def fetch(f: FetchOptions => FetchOptions): Find[T] = Find(q, f(fo))

  def iterable(ds: DatastoreService)(implicit ec: EntityCreatable[T]): Iterable[Keyed[T]] = 
    ds.prepare(q).asIterable(fo) flatMap (e => e.create[T])
}

object Find {
  private def defaultOptions = FetchOptions.Builder.withChunkSize(FetchOptions.DEFAULT_CHUNK_SIZE)

  def apply[T](implicit k: Kind[T]): Find[T] = Find(gae.createQuery[T], defaultOptions)
}

trait FindDSL {
  val field: String
  type QRY = Query => Query

  import com.google.appengine.api.datastore.Query.SortDirection._
  import com.google.appengine.api.datastore.Query.FilterOperator._

  def asc: QRY = (_.addSort(field, ASCENDING))
  def desc: QRY = (_.addSort(field, DESCENDING))
  def ?==[T](t: T): QRY = (_.addFilter(field, EQUAL, t))

  def ?!=[T](t: T): QRY = (_.addFilter(field, NOT_EQUAL, t))
}

trait FindDSLImplicits {
  implicit def stringTo(s: String): FindDSL = new FindDSL { val field = s }
  implicit def stringFrom(dsl: FindDSL): String = dsl.field
}

object dsl extends FindDSLImplicits