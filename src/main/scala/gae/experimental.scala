package wd
package experimental

import scalaz._
import Scalaz._
import com.google.appengine.api.datastore._
import scala.collection.JavaConversions._

  
object Breweries extends EntityBase[(String, String)] {
  val kind = "brewery"
  def * = "name".prop[String] ~ "country".prop[String]
}

object test {
  case class StringPair(s1: String, s2: String)
  object mapped extends Base[StringPair]("pair") {
    def * = "s1".prop[String] ~ "s2".prop[String] <> (StringPair, StringPair.unapply _)
  }
  
  def t1 {
    mapped.* 
  }
}
