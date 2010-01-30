package wd

import scalaz._
import Scalaz._
import gae._
import com.google.appengine.api.datastore._

case class Style(value: String) extends NewType[String]

case class Beer(name: String, style: Style)

object BeerModel extends Model[Beer] {
  def entityBase = UnnamedClassEntityBase[Beer]
  def entityCreatable = entityCreate2(Beer.apply _, ("name", "style"))
  
  def entityWriteable = new EntityWriteable[Beer] {
    def write(b: Beer, e: Entity) {
      e.setProperty("name", b.name)
      e.setProperty("style", b.style.value)
    }
  }
}