package wd

import com.google.appengine.api.datastore._
import com.google.appengine.api.datastore.Query.SortDirection
import com.google.appengine.api.datastore.Query.SortDirection._
import com.google.appengine.api.datastore.Query.FilterOperator._
import gae._
import scalaz._
import Scalaz._

// Location
// * full address.
// isPub

case class Country(value: String) extends NewType[String]
case class Brewery(name: String, country: Country)

object Brewery {
  
  implicit def dsl(field: String) = new {
    import com.google.appengine.api.datastore.Query.SortDirection._
    import com.google.appengine.api.datastore.Query.FilterOperator._
    
    type QRY = Query => Query
    
    def asc: QRY = (_.addSort(field, ASCENDING))
    def ?==[T](t: T): QRY = (_.addFilter(field, EQUAL, t))
  }
  
  val allByName = Find[Brewery].query("name".asc).iterable _
  
  def allInCountry(c: Country) = Find[Brewery].query("country" ?== c.value).iterable _
}
