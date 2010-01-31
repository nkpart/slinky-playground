package scapps

import scalaz._
import Scalaz._
import scalaz.http.Slinky._
import scalaz.http._
import scalaz.http.request._
import scalaz.http.request.Request._
import scalaz.http.response._
import xml.NodeSeq
import gae._
import rest._

trait Controller {
  val layout: (NodeSeq => NodeSeq) // TODO strong type of layout. so the implicit is more explicit.
  
  implicit val charset: CharSet // Needed to create a response, force it here because it's easy to forget

  def render(ns: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = respond(OK, layout(ns))

  def render(status: Status, ns: NodeSeq)(implicit r: Request[Stream]): Response[Stream] = respond(status, layout(ns))
  
  private def respond(status: Status, body: NodeSeq)(implicit r: Request[Stream]): Response[Stream] =
    status(ContentType, "text/html") << transitional << body
}
