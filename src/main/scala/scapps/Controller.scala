package scapps

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
import gae._
import RichNodeSeq._
import rest.Resourced

trait Controller {
  val layout: (NodeSeq => NodeSeq) // TODO strong type of layout. so the implicit is more explicit.
  implicit val charset: CharSet // Needed to create a response, force it here because it's easy to forget

  def render(ns: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = respond(OK, layout(ns))

  def render(status: Status, ns: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = respond(status, layout(ns))

  def redirectTo(l: String)(implicit r: Request[Stream]): Response[Stream] = Response.redirects(l)
  
  def redirectTo[T](t: T)(implicit r: Request[Stream], resourced: Resourced[T]): Response[Stream] = Response.redirects(resourced.show(t))

  private def respond(status: Status, body: NodeSeq)(implicit r: Request[Stream]): Response[Stream] =
    status(ContentType, "text/html") << transitional << body
}
