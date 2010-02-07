package scapps

import belt._
import scalaz.http.servlet._
import scala.util.DynamicVariable

object R {
  private val requestData = new DynamicVariable[Request](null)
  
  implicit def request = requestData.value
  
  def service[T](request: Request)(t: => T): T = {
    requestData.withValue(request)(t)
  }
}