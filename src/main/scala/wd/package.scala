import com.google.appengine.api.datastore.{Key, DatastoreService, KeyFactory, Entity}
import gae._
import rest._
import scalaz._
import Scalaz._
import scapps._
import scalaz.http.request._
import wd.{Brewery, Beer, Style, Country}
import prohax.Inflector._
import sage._

package object wd extends RequestImplicits {
  type Request[IN[_]] = scalaz.http.request.Request[IN]
  type Response[IN[_]] = scalaz.http.request.Request[IN]
  
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
}
