import com.google.appengine.api.datastore.{Key, DatastoreService, KeyFactory}
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

  implicit def beerStored = new Stored[Beer] {
    val kind: String = classKind[Beer]

    def keyName(b: Beer) = b.name.split(" ").reduceLeft(_ + "-" + _)

    def withKey(t: Beer, k: Key): Beer = {
      t copy (key = Some(k))
    }
  }

  implicit def breweryStored = new Stored[Brewery] {
    val kind: String = classKind[Brewery]

    def keyName(b: Brewery) = b.name.split(" ").reduceLeft(_ + "-" + _)

    def withKey(t: Brewery, k: Key): Brewery = {
      t copy (key = Some(k))
    }
  }


  implicit def br = new Resourced[Brewery] {
    val resource: Resource = Resource("breweries")

    def id(br: Brewery): String = br.key match {
      case Some(key) => key.getName
      case None => error("cannot form id from unsaved brewery")
    }
  }
}
