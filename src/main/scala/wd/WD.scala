import com.google.appengine.api.datastore.{Key, DatastoreService, KeyFactory, Entity}
import gae._
import rest._
import scalaz._
import Scalaz._
import scapps._
import scalaz.http.request._
import wd.{Brewery, Beer}
import prohax.Inflector._

package object wd extends RichRequests {
  type DB[T] = (DatastoreService => T)
  //TODO move to scapps
  type NamedError = (String, String)
  type Posted[T] = Validation[NonEmptyList[NamedError], T]

  def UnnamedClassKeyFor[T](implicit m: ClassManifest[T]) = new KeyFor[T] {
    def kind = m.erasure.getSimpleName
    def keyName(t: T) = None
  }
  
  def KeyedResource[T](implicit m: ClassManifest[T]) = new Resourced[Keyed[T]] {
    val resource: Resource = Resource(m.erasure.getSimpleName.toLowerCase.pluralize)
    def id(kt: Keyed[T]): String = kt.key.getId.shows
  }

  implicit val beerKey = UnnamedClassKeyFor[wd.Beer]
  implicit val breweryKey = UnnamedClassKeyFor[wd.Brewery]
  
  implicit def br = KeyedResource[Brewery]
  
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
  
  implicit val brewPost = new RequestCreate[Brewery] with RequestUpdate[Brewery] with scapps.Validations {
    val Required = "%s is required."

    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required)
      name ∘ { Brewery.apply _ }
    }

    def update[IN[_]: FoldLeft](r: Request[IN])(brewery: Brewery) = {
      val name = required(r)("name")(Required)
      name ∘ { n => brewery copy (name = n) } fold (errs => (errs.list, brewery), (Nil, _))
    }
  }

  implicit val beerPost = new RequestCreate[Beer] with RequestUpdate[Beer] with scapps.Validations {
    val Required = "%s is required."
    
    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required)
      name ∘ {n => Beer(n)}
    }

    def update[IN[_] : FoldLeft](r: Request[IN])(beer: Beer) = {
      val name = required(r)("name")(Required)
      name ∘ {n => beer copy (name = n)} fold (err => (err.list, beer), br => (Nil, br))
    }
  }

  implicit def createFromEntity: EntityCreatable[Brewery] = new EntityCreatable[Brewery] {
    def createFrom(e: Entity): Option[Brewery] = {
      val name = Option(e.getProperty("name").asInstanceOf[String])
      name map (n => Brewery(n))
    }
  }

  implicit def createFromBeerEntity: EntityCreatable[Beer] = new EntityCreatable[Beer] {
    def createFrom(e: Entity): Option[Beer] = {
      val name = Option(e.getProperty("name").asInstanceOf[String])
      name map (n => Beer(n))
    }
  }
}
