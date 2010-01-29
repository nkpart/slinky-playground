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
