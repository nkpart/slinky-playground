package scapps

import scalaz._
import Scalaz._

import belt._
import scapps.RichRequest._

trait Validations {
  private def fail(fldName: String, message: String) = (fldName -> message.format(fldName))
  
  def required(r: Request)(fldName: String)(fmt: String): Validation[NonEmptyList[(String,String)], String] =
    r(fldName).toSuccess(fail(fldName, fmt).wrapNel)
    
  def nonEmpty(r: Request)(fldName: String)(fmt: String): Validation[NonEmptyList[(String,String)], String] =
    required(r)(fldName)(fmt) >>= (((s: String) => !s.isEmpty).toValidation(fail(fldName, fmt)))   
}
