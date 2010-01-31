package scapps

import scalaz._
import http.request._
import http.request.Request._
import http.response.Response
import Scalaz._
import rest._

trait RequestW[IN[_]] {
  val request: Request[IN]
  
  def apply(s: String)(implicit f: FoldLeft[IN]): Option[String] = (request !| s) ∘ (_.mkString)

  def create[T](implicit postable: RequestCreate[T], fl: FoldLeft[IN]) = postable.create(request)

  def update[T](t: T)(implicit postable: RequestUpdate[T], fl: FoldLeft[IN]) = postable.update(request)(t)
  
  def log {
    println("%s\t%s" format (request.method, request.path.list.mkString))
  }
  
  def redirectTo(place: String)(implicit e: Empty[IN]) = Response.redirects(place)(e,request)
  
  def redirectTo[T](t: T)(implicit e: Empty[IN], resourced: Resourced[T]): Response[IN] = Response.redirects(resourced.show(t))(e, request)
  
  def methodHax(methodField: String = "_method")(implicit f: FoldLeft[IN]): Request[IN] = {
    val method = (apply(methodField) >>= (scalaz.http.Slinky.StringMethod _)) 
    method ∘ { (method :Method) => request(method) } | request
  }
  
  def addQueryParam[IN[_]](key: String, value: String) = {
    val kv = (key + "=" + value).toList // TODO escape key and value
    val newUri = request.line.uri ++++ {
      _ match {
        case Some(qs) => Some(qs ++ ('&' :: kv))
        case None => Some(kv)
      }
    }
    request(request.line(newUri))
  }
}

trait RequestImplicits {
  implicit def To[IN[_]](r: Request[IN]) = new RequestW[IN] { val request = r }
  
  implicit def From[IN[_]](r: RequestW[IN]) = r.request
}

object RichRequest extends RequestImplicits
