import com.google.appengine.api.datastore.{Key, DatastoreService, KeyFactory, Entity}
import gae._
import rest._
import scalaz._
import Scalaz._
import scapps._
import scalaz.http.request._
import wd.{Brewery, Beer, Style, Country}
import prohax.Inflector._

package object wd extends RequestImplicits {
  type DB[T] = (DatastoreService => T)
  //TODO move to scapps
  type NamedError = (String, String)
  type Posted[T] = Validation[NonEmptyList[NamedError], T]

  def UnnamedClassEntityBase[T](implicit m: ClassManifest[T]) = new EntityBase[T] {
    def kind = m.erasure.getSimpleName
    def keyName(t: T) = None
  }
  
  def KeyedResource[T](implicit m: ClassManifest[T]) = new Resourced[Keyed[T]] {
    val resource: Resource = Resource(m.erasure.getSimpleName.toLowerCase.pluralize)
    def id(kt: Keyed[T]): String = kt.key.getId.shows
  }

  implicit val beerModel: Model[Beer] = BeerModel
  implicit val breweryModel: Model[Brewery] = BreweryModel
  
  implicit def breweryR = KeyedResource[Brewery]
  implicit def beerR = KeyedResource[Beer]
  
  implicit val brewPost = new RequestCreate[Brewery] with RequestUpdate[Brewery] with scapps.Validations {
    val Required = "%s is required."

    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required)
      val country = required(r)("country")(Required)
      (name <|*|> country) ∘ { case (n,c) => Brewery(n, Country(c)) }
    }

    def update[IN[_]: FoldLeft](r: Request[IN])(brewery: Brewery) = {
      val name = required(r)("name")(Required)
      val country = required(r)("country")(Required)
      (name <|*|> country) ∘ { case (n,c) => brewery copy (name = n, country = Country(c)) } fold (errs => (errs.list, brewery), (Nil, _))
    }
  }

  implicit val beerPost = new RequestCreate[Beer] with RequestUpdate[Beer] with scapps.Validations {
    val Required = "%s is required."
    
    def create[IN[_] : FoldLeft](r: Request[IN]) = {
      val name = required(r)("name")(Required)
      val style = required(r)("style")(Required)
      (name <|*|> style) map { case (n,s) => Beer(n, Style(s))}
    }

    def update[IN[_] : FoldLeft](r: Request[IN])(beer: Beer) = {
      val name = required(r)("name")(Required)
      val style = required(r)("style")(Required)
      
      (name <|*|> style) map { case (n,s) => beer copy (name = n, style = Style(s))} fold (err => (err.list, beer), br => (Nil, br))
    }
  } 

  def entityCreate2[T, A, B](cons: (A, B) => T, fields: (String, String)): EntityCreatable[T] = new EntityCreatable[T] {
    def createFrom(e: Entity): Option[T] = {
      val va = Option(e.getProperty(fields._1).asInstanceOf[A])
      val vb = Option(e.getProperty(fields._2).asInstanceOf[B])
      (va <|*|> vb) map cons.tupled
    }
  }  
}
