package scapps

import belt._
import scala.util.DynamicVariable

object R {
  private val requestVar = new DynamicVariable[Request](null)
  
  def request = requestVar.value

  def set(request: Request) = requestVar.value = request

  def service[T](request: Request)(t: => T): T = {
    requestVar.withValue(request)(t)
  }
}