package scapps

import scalaz._
import Scalaz._
import scalaz.http.Slinky._
import scalaz.http._
import response.{OK, Status}
import xml.NodeSeq
import gae._
import rest._
import belt._

trait Layout {
  def layout(content: NodeSeq): NodeSeq // TODO strong type of layout. so the implicit is more explicit.
  
  implicit val charset: CharSet // Needed to create a response, force it here because it's easy to forget

  def render(ns: NodeSeq, status: Status = OK): Response = respond(status, layout(ns))
  
  private def respond(status: Status, body: NodeSeq): Response = Response(status, (ContentType, "text/html"))(resp => {
    resp << transitional << body
  })
}
