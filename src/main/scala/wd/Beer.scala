package wd

import scalaz._
import Scalaz._
import gae._
//import com.google.appengine.api.datastore._
import sage._

case class Style(value: String) extends NewType[String]

case class Beer(name: String, style: Style)

object Beers extends sage.Base[Beer]("beer") {
  def * = "name".prop[String] ~ "style".typedProp(Style) <> (Beer, Beer.unapply _)
}
