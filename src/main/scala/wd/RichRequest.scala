package wd

import scalaz.http.request.Request
import scalaz._
import Scalaz._


trait RichRequest[IN[_]] {
  val request: Request[IN]
  def apply(s: String)(implicit f: FoldLeft[IN]): Option[String] = (request !| s) ∘ (_.mkString)
}

object RichRequest {
  implicit def To[IN[_]](r: Request[IN]) = new RichRequest[IN] { val request = r }
  implicit def From[IN[_]](r: RichRequest[IN]) = r.request
}
