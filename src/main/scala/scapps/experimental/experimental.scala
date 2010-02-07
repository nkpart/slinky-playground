package scapps

import xml._
import scalaz.http._
import response.OK
import scalaz.http.Slinky._
import scalaz._
import Scalaz._
import rest._
import belt._

package object experimental {
  def right[A,B](b: B): Either[A,B] = Right(b)
  def left[A,B](a: A): Either[A,B] = Left(a)
  
  trait RestPull {
    def apply(v: rest.Action[String]): Option[Response]
    
    def tap(expectedBase: String)(r: Request): Either[Request,Response] = {
      (r.action.filter(_._1 == expectedBase) >>= ((pair: (String, rest.Action[String])) => {
        apply(pair._2) map { r => right[Request,Response](r) }
      })) getOrElse { left(r) }
    }
  }

  type Tap = Request => Either[Request, Response]
  type Filter = Request => Option[Request]
  type Pull = Request => Option[Response]

  type Drink = Request => Response

  trait ContentTypeFor[T] {
    val get: String
  }

  trait ReadingRenders {
    implicit val charset = UTF8

    def render[T](t: T)(implicit ctf: ContentTypeFor[T]): Response = Response(OK, (ContentType, ctf.get))
  }

  trait MutableRouteDSL extends ReadingRenders {
    def ctf[T](s : String) = new ContentTypeFor[T] { val get = s }
    implicit def nsCT: ContentTypeFor[NodeSeq] = ctf("text/xhtml")
    implicit def elemCT: ContentTypeFor[Elem] = ctf("text/xhtml")
    implicit def stringHtmlCT: ContentTypeFor[String] = ctf("text/html")

    val taps: scala.collection.mutable.ArrayBuffer[Tap] = new scala.collection.mutable.ArrayBuffer[Tap]

    var lastDrink: Response = render("404 - not found")

    def resource(resourceName: String)(pf: PartialFunction[Action[String], Response]) {
      val f = pf.lift
      taps += (new RestPull { def apply(v: rest.Action[String]) = f(v)}.tap(resourceName) _)
    }

    def tap: Drink = { (r: Request) =>
      taps.foldLeft(Left(r): Either[Request, Response]) { (r, tap) =>
        (r.left >>= (tap ∘ (_.left))).e
      }.left map (_ => lastDrink) merge
    }
  }
  
  object routes extends MutableRouteDSL  {
    resource("breweries") { case New =>
      render(<p>Woo</p>)
    }
  }
}

