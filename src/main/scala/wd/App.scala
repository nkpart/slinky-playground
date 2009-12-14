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
import xml.transform._
import xml._
import implicits._

object implicits {
  implicit def richSeq(n: NodeSeq) = new RichNodeSeq {val ns = n}
}

object WD extends Web[Stream] {
  implicit val charset = UTF8
  val layout = layouts.main

  val Index = new Bit[Unit] {
    def action = const(())

    def view = ( implicit request => _ => {
      //~respond match { case 'json => ()}
      respond {
        <h1>Hi mum</h1>
      }
    })
  }

  def Show(s: String) = new Bit[(String, Int)] {
    def action = const((s, 26))
    def view = ( implicit request => dataz => {
      respond {
        dataz.toString.text
      }
    })
  }

  def route(request: Request[Stream]): Option[Response[Stream]] = {
    request match {
      case MethodParts(GET, Nil) => Index.f(request) η
      case MethodParts(GET, "show" :: s :: Nil) => Show(s).f(request) η
      case _ => None
    }
  }
}

final class App extends StreamStreamServletApplication {
  implicit val charset = UTF8

  def handle(implicit request: Request[Stream], servletRequest: HttpServletRequest): Option[Response[Stream]] = {
    println(MethodParts.unapply(request))
    WD.route(request)
  }


  val application = new ServletApplication[Stream, Stream] {
    def application(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[Stream]) = {
      handle getOrElse resource(x => OK << x.toStream, NotFound.xhtml)
    }
  }
}
