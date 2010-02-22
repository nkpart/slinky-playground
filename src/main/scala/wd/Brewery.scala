package wd

import gae._
import sage._
import scalaz._
import Scalaz._
import scapps._
import belt._
import sage.http.StringW._
import com.google.appengine.api.datastore._

// TODO
// - full address
// - isPub

case class Brewery(name: String, country: String)

object Breweries extends Base[Brewery]("breweries") {
  def * = "name".prop[String] :: "country".prop[String] >< (Brewery <-> Brewery.unapply _)

  import sage.dsl._
  def allByName(implicit ds: DatastoreService) = this.find.query("name".asc).iterable
  def allInCountry(c: String)(implicit ds: DatastoreService) = this.find.query("country" ?== c).iterable
}

object BreweryPost extends RequestCreate[Brewery] with RequestUpdate[Brewery] with scapps.Validations {
  val Required = "%s is required."
//  val name = "name".as[String] --> nonEmpty

  def create(r: Request) = {
//    ("name".as[String] :: "country".as[String] >< (Brewery <-> Brewery.unapply _)).get(r.underlying)
    
    val name = required(r)("name")(Required)
    val country = nonEmpty(r)("country")(Required)
    (name <|*|> country) ∘ { case (n,c) => Brewery(n, c) }
  }

  def update(r: Request)(brewery: Brewery) = {
    val name = required(r)("name")(Required)
    val country = nonEmpty(r)("country")(Required)
    (name <|*|> country) ∘ { case (n,c) => brewery copy (name = n, country = c) } fold (errs => (errs.list, brewery), (Nil, _))
  }
}