package wd

import com.google.appengine.api.datastore._
import com.google.appengine.api.datastore.Query.SortDirection
import com.google.appengine.api.datastore.Query.SortDirection._
import com.google.appengine.api.datastore.Query.FilterOperator._
import gae._
import gae.dsl._
import scalaz._
import Scalaz._

// TODO
// - full address
// - isPub

case class Country(value: String) extends NewType[String]
case class Brewery(name: String, country: Country)

object Brewery {
  val allByName = Find[Brewery].query("name".asc).iterable _
  
  def allInCountry(c: Country) = Find[Brewery].query("country" ?== c.value).iterable _
}
