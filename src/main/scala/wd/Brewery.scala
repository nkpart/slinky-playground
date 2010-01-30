package wd

import gae._
import gae.dsl._
import scalaz._
import Scalaz._
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