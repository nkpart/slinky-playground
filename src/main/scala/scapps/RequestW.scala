package scapps

import belt._
import scalaz._
import Scalaz._
import rest._

trait RequestW {
  val request: Request
  
  def create[T](implicit postable: RequestCreate[T]) = postable.create(request)

  def update[T](t: T)(implicit postable: RequestUpdate[T]) = postable.update(request)(t)
  
  def formBase(e: List[(String, String)]) = new FormBase {
    val errors = e
    val previous = ((key: String) => (request(key)) map (_.mkString))
  }
}

trait RequestImplicits {
  implicit def To(r: Request) = new RequestW { val request = r }
  
  implicit def From(r: RequestW) = r.request
}

object RichRequest extends RequestImplicits
