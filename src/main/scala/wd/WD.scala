import gae._
import rest._
import wd.{Brewery, Beer}

package object wd {
  import Kind._
  implicit def beerKind = classKind[Beer]
  implicit def breweryKind = classKind[Brewery]

  implicit def br = new Resourced[Brewery] {
    val resource: Resource = Resource("brewery")
    def id(br: Brewery): String = br match {
      case Brewery(name, Some(key)) => key.toString
      case _ => error("cannot form id from unsaved brewery")
    }
  }
}
