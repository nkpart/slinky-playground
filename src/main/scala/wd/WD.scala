import com.google.appengine.api.datastore.{Key, DatastoreService, KeyFactory, Entity}
import gae._
import rest._
import scalaz._
import Scalaz._
import scapps.RichRequests
import wd.{Brewery, Beer}

package object wd extends RichRequests {
  type DB[T] = (DatastoreService => T)
  //TODO move to scapps
  type NamedError = (String, String)
  type Posted[T] = Validation[List[NamedError], T]
  
  implicit object beerKey extends KeyFor[wd.Beer] {
    def kind = "Beer"
    def keyName(bb: wd.Beer) = None
  }

  implicit object breweryKey extends KeyFor[wd.Brewery] {
    def kind = "Brewery"
    def keyName(b: wd.Brewery) = None
   }

  implicit def br = new Resourced[Keyed[Brewery]] {
    val resource: Resource = Resource("breweries")
    def id(br: Keyed[Brewery]): String = br.key.getId.shows
  }
  
  implicit object breweryEntityThing extends EntityWriteable[wd.Brewery] {
    def write(b: Brewery, e: Entity) {
      e.setProperty("name", b.name)
    }
  }
  
  implicit object beerEntityThing extends EntityWriteable[wd.Beer] {
    def write(b: Beer, e: Entity) {
      e.setProperty("name", b.name)
    }
  }
}
