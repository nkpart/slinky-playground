package wd

import scalaz._
import Scalaz._

case class Style(value: String) extends NewType[String]

case class Beer(name: String, style: Style)

