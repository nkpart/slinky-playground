package wd
package controllers

import scalaz._
import Scalaz._
import scalaz.http.response._
import scalaz.http.request._
import wd.views.layouts
import xml.NodeSeq
import scapps._

trait WDLayout extends Layout {
  implicit val charset = UTF8

  def layout(ns: NodeSeq) = layouts.main(gae.Services.userService)(ns)
}

trait RestController[T] extends WDLayout {
  def apply(v: rest.Action[T]): Option[Response[Stream]]
}

