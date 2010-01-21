package scapps

import scalaz._
import http.request._
import http.request.Request._
import Scalaz._
import rest._

trait RichRequest[IN[_]] {
  val request: Request[IN]
  def apply(s: String)(implicit f: FoldLeft[IN]): Option[String] = (request !| s) ∘ (_.mkString)

  def create[T](implicit postable: RequestCreate[T], fl: FoldLeft[IN]) = postable.create(request)

  def update[T](t: T)(implicit postable: RequestUpdate[T], fl: FoldLeft[IN]) = postable.update(request)(t)

  lazy val action: Option[(String, Action[String])] = {
    request match {
      case MethodParts(GET, List(base)) => Some((base, rest.Index))
      case MethodParts(GET, List(base, "new")) => Some((base, New))
      case MethodParts(GET, List(base, id)) => Some((base, rest.Show(id)))
      case MethodParts(POST, List(base)) => Some((base, Create))
      case MethodParts(GET, List(base, id, "edit")) => Some((base, Edit(id)))
      case MethodParts(PUT, List(base, id)) => Some((base, Update(id)))
      case MethodParts(DELETE, List(base, id)) => Some((base, Destroy(id)))
      case _ => none
    }
  }
}

trait RichRequests {
  implicit def To[IN[_]](r: Request[IN]) = new RichRequest[IN] { val request = r }
  implicit def From[IN[_]](r: RichRequest[IN]) = r.request
}

object RichRequest extends RichRequests
