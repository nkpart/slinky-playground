import com.google.appengine.api.datastore._
import gae._
import rest._
import scalaz._
import Scalaz._
import scapps._
import belt._
import wd.{Brewery, Beer, Style, Country}
import prohax.Inflector._
import sage._

package object wd extends RequestImplicits {
  implicit val charset = UTF8
  //TODO move to scapps
  type NamedError = (String, String)
  type Posted[T] = Validation[NonEmptyList[NamedError], T]
  
  def KeyedResource[T](implicit m: ClassManifest[T]) = new Resourced[Keyed[T]] {
    val resource: Resource = Resource(m.erasure.getSimpleName.toLowerCase.pluralize)
    def id(kt: Keyed[T]): String = kt.key.getId.shows
  }

  implicit def breweryR = KeyedResource[Brewery]
  implicit def beerR = KeyedResource[Beer]
  
  implicit val breweryPost = BreweryPost

  implicit val beerPost = new RequestCreate[Beer] with RequestUpdate[Beer] with scapps.Validations {
    val Required = "%s is required."
    
    def create(r: Request) = {
      val name = required(r)("name")(Required)
      val style = required(r)("style")(Required)
      (name <|*|> style) map { case (n,s) => Beer(n, Style(s))}
    }

    def update(r: Request)(beer: Beer) = {
      val name = required(r)("name")(Required)
      val style = required(r)("style")(Required)
      
      (name <|*|> style) map { case (n,s) => beer copy (name = n, style = Style(s))} fold (err => (err.list, beer), br => (Nil, br))
    }
  } 
}
