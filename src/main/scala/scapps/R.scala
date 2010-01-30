package scapps

import scalaz.http.request._
import scalaz.http.servlet._
import scala.util.DynamicVariable

object R {
  private val requestData = new DynamicVariable[(Request[Stream], HttpSession)](null)
  
  implicit def request = requestData.value._1
  def session = requestData.value._2 
  
  def service[T](request: Request[Stream], session: HttpSession)(t: => T): T = {
    requestData.withValue((request,session))(t)
  }
}