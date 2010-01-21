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
  
  implicit object beerKind extends Kind[Beer] {
    def kind = "Beer"
  }
  
  implicit object beerKey extends KeyFor[(wd.Beer, wd.Brewery)] {
    def kind = beerKind.kind
    def keyFor(b: (wd.Beer, wd.Brewery)) = b._1.key
    def keyName(bb: (wd.Beer, wd.Brewery)) = None
    def parentKey(bb: (wd.Beer, wd.Brewery)) = Some(bb._2.key.get) // force exception. can only get key for saved brewery
    def withKey(bb: (wd.Beer, wd.Brewery), k: Key) = (bb._1 copy (key = Some(k)), bb._2)
  }

  implicit object breweryKey extends KeyFor[wd.Brewery] {
    def keyFor(b: wd.Brewery) = b.key
    def kind = "Brewery"
    def keyName(b: wd.Brewery) = None
    def parentKey(b: wd.Brewery) = None
    def withKey(b: wd.Brewery, k: Key) = b copy (key = Some(k))
   }

  implicit def br = new Resourced[Brewery] {
    val resource: Resource = Resource("breweries")
    def id(br: Brewery): String = br.key match {
      case Some(key) => key.getId.shows
      case None => error("cannot form id from unsaved brewery")
    }
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
