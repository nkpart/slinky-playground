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
import gae.Gae._
import RichNodeSeq._

trait Web[IN[_]] {
  // TODO strong type of layout. so the implicit is more explicit.
  implicit val layout: NodeSeq
  implicit val charset: CharSet

  def respond(body: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = {
    val cleaned = body.mapTree(_ match {
      case e if e.prefix == "slinky" => NodeSeq.Empty
      case n => n
    })
    OK(ContentType, "text/html") << transitional << cleaned
  }

  def inLayout(ns: NodeSeq)(implicit layout: NodeSeq) = {
    layout.replaceAll(<slinky:yield/>, ns)
  }

  def render(ns : NodeSeq)(implicit r : Request[Stream]) : Response[Stream] = {
    respond(inLayout(ns))
  }
}
