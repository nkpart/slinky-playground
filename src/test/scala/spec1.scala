import org.specs._

import wd._
import experimental._

import java.lang.{Integer => JInt}

object Simple extends Base[String]("simples") {
  def * = "simple".prop[String]
}

object StringInt extends Base[(String,JInt)]("stringint") {
  def * = "a".prop[String] ~ "b".prop[JInt]
}

class Datastore extends Specification {
  "reading" in {
    Simple.read(Map[String,Object]("simple" -> "a")) must_== Some("a")
    StringInt.read(Map[String,Object]("a" -> "value a", "b" -> (5:Integer))) must_== Some(("value a", 5))
  }
  
  "reading . writing == id" in {
    Simple.read(Simple.write("a")) must_== Some("a")
    StringInt.read(StringInt.write(("value a", 5))) must_== Some(("value a", 5))
  }
  
  "type checking on reading" in {
    Simple.read(Map[String,Object]("simple" -> (5:Integer))) must_== None
  }
  
  "okay so" in {
    val result: (Key, String) = Simple.save("lol")
  }
}
