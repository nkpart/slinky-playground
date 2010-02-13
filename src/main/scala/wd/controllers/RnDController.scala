package wd.controllers

import rest._
import belt._
import scalaz.http.response.OK

import scala.collection.mutable.{Map => MMap}

trait RenderArgs {
  val renderArgs: MMap[String, Any] = MMap()

  implicit def pimpedString(s: String) = new {
    def <+(a: Any) = renderArgs += (s -> a)
  }
}
