import com.google.appengine.api.datastore.{DatastoreService, KeyFactory}
import gae._
import rest._
import scalaz.Validation
import scapps.RichRequests
import wd.{Brewery, Beer}

package object wd extends RichRequests {
  type DB[T] = (DatastoreService => T)
  //TODO move to scapps
  type NamedError = (String, String)
  type Posted[T] = Validation[List[NamedError], T]

  implicit def beerKind = classKind[Beer]
  implicit def breweryKind = classKind[Brewery]

  implicit def br = new Resourced[Brewery] {
    val resource: Resource = Resource("breweries")
    def id(br: Brewery): String = br.key match {
      case Some(key) => key.getName
      case None => error("cannot form id from unsaved brewery")
    }
  }
}
