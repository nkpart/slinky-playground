package scapps

import scalaz.http.request._
import scala.xml.NodeSeq
import scalaz._
import Scalaz._

trait FormBase {
  val errors: List[(String,String)]
  val previous: String => Option[String]
  
  // Creates a new text field. value will be overriden by any previous values.
  def text(name: String, value: Option[String] = None, cls: Option[String] = None, id: Option[String] = None) = {
    val v = ~(previous(name) orElse value)
    <input name={name} type="text" value={v} class={cls getOrElse null} />
  }
  
  def form(action: String, method: Method)(ns: NodeSeq) = {
    <form action={action} method={method}> // TODO: apply method hax.
      {ns}
    </form>
  }
  
  def select(name: String, options: List[(String,String)]) = {
    <select name="brewery_id">
      {options flatMap (o => <option value={o._1}>{o._2}</option>)}
    </select>
  }
}