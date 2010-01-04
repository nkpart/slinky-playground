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

trait Controller {
  val layout: (NodeSeq => NodeSeq)   // TODO strong type of layout. so the implicit is more explicit.
  implicit val charset: CharSet // Needed to create a response, force it here because it's easy to forget

  def render(ns : NodeSeq)(implicit r : Request[Stream]) : Response[Stream] = {
    respond(layout(ns))
  }
  
  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)

  private def respond(body: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = {
    val cleaned = body.mapTree(_ match {
      case e if e.prefix == "slinky" => NodeSeq.Empty
      case n => n
    })
    OK(ContentType, "text/html") << transitional << cleaned
  }

  private def inLayout(ns: NodeSeq) = {
    layout(ns)
  }
}
