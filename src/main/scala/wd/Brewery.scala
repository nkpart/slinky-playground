package wd

import gae._
import gae.dsl._
import scalaz._
import Scalaz._
import scapps._
import com.google.appengine.api.datastore._

// TODO
// - full address
// - isPub

case class Country(value: String) extends NewType[String]

case class Brewery(name: String, country: Country)

object Brewery {
  val allByName = Find[Brewery].query("name".asc).iterable _
  
  def allInCountry(c: Country) = Find[Brewery].query("country" ?== c.value).iterable _
}

object BreweryModel extends Model[Brewery] {
  def entityBase = UnnamedClassEntityBase[Brewery]

  def entityCreatable = entityCreate2[Brewery, String, String](
    (n,c) => { Brewery(n, Country(c)) },
    ("name", "country")
  )
  
  def entityWriteable = new EntityWriteable[Brewery] {
    def write(b: Brewery, e: Entity) {
      e.setProperty("name", b.name)
      e.setProperty("country", b.country.value)
    }
  }
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