package wd.controllers

import wd.{layouts, Web}
import scalaz.Scalaz._
import scalaz.http.Slinky._
import scalaz.http.request._
import scalaz.http.response._

object Home extends Web[Stream] {
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
      case MethodParts(GET, Nil) => Index.f(request) ?
      case MethodParts(GET, "show" :: s :: Nil) => Show(s).f(request) ?
      case _ => None
    }
  }
}
