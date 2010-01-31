package scapps

import scalaz._
import Scalaz._
import scalaz.http.request._
import scalaz.http.response._
import scapps.RichRequest._

trait Validations {
  private def fail(fldName: String, message: String) = (fldName -> message.format(fldName))
  
  def required[IN[_]: FoldLeft](r: Request[IN])(fldName: String)(fmt: String): Validation[NonEmptyList[(String,String)], String] = 
    r(fldName).toSuccess(fail(fldName, fmt).wrapNel)
    
  def nonEmpty[IN[_]: FoldLeft](r: Request[IN])(fldName: String)(fmt: String): Validation[NonEmptyList[(String,String)], String] =
    required(r)(fldName)(fmt) >>= (((s: String) => !s.isEmpty).toValidation(fail(fldName, fmt)))
    
}