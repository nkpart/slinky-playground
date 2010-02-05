package wd

import gae._
import sage._
import scalaz._
import Scalaz._
import scapps._
import com.google.appengine.api.datastore._

// TODO
// - full address
// - isPub

case class Country(value: String) extends NewType[String]

case class Brewery(name: String, country: Country)

object Breweries extends Base[Brewery]("breweries") {
  def * = "name".prop[String] ~ "country".typedProp(Country) <> (Brewery, Brewery.unapply _)

  import sage.dsl._
  
  def allByName(implicit ds: DatastoreService) = this.find.query("name".asc).iterable
  
  def allInCountry(c: Country)(implicit ds: DatastoreService) = this.find.query("country" ?== c.value).iterable
}


object BreweryPost extends RequestCreate[Brewery] with RequestUpdate[Brewery] with scapps.Validations {
  val Required = "%s is required."

  def create[IN[_] : FoldLeft](r: Request[IN]) = {
    val name = required(r)("name")(Required)
    val country = nonEmpty(r)("country")(Required)
    (name <|*|> country) ∘ { case (n,c) => Brewery(n, Country(c)) }
  }

  def update[IN[_]: FoldLeft](r: Request[IN])(brewery: Brewery) = {
    val name = required(r)("name")(Required)
    val country = nonEmpty(r)("country")(Required)
    (name <|*|> country) ∘ { case (n,c) => brewery copy (name = n, country = Country(c)) } fold (errs => (errs.list, brewery), (Nil, _))
  }
}