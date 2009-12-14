package wd

import Function._
import scalaz.Scalaz._
import scalaz.http.Slinky._
import scalaz._
import http._
import scalaz.http.request._
import scalaz.http.request.Request._
import scalaz.http.response._
import scalaz.http.servlet._
import scalaz.http.servlet.HttpServlet.resource
import xml.NodeSeq

trait Web[IN[_]] {
  trait Bit[VT] {
    type Action = Request[IN] => VT
    type View = Request[IN] => VT => Response[IN]

    def action: Action
    def view: View

    // TODO ?
    def f : Request[IN] => Response[IN] = request => view(request)(action(request))
  }

  val layout : NodeSeq
  implicit val charset : CharSet

  def route(request: Request[IN]): Option[Response[IN]]


  def respond(body: NodeSeq)(implicit r: Request[Stream]) = {
    val doc = layout.replaceAll(<slinky:yield/>, body)
    val cleaned = doc.mapTree(_ match {
      case e if e.prefix == "slinky" => NodeSeq.Empty
      case n => n
    })
    OK(ContentType, "text/html") << transitional << cleaned
  }
}
