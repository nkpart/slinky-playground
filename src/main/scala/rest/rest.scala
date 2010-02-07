import rest.Resourced
import scalaz._
import belt._
import http.request.Request._
import http.request.{Request => _, GET, PUT, POST, DELETE}
import Scalaz._

package object rest {
  
  def resource(base: String, f: (Request => Action[String] => Option[Response])) = ☆((r: Request) => {
    r.action match {
      case Some((b, action)) if b == base => f(r)(action)
      case _   => none
    }
  })

  def redirect[T](t: T)(implicit resourced: Resourced[T]): belt.Response = belt.redirect(resourced.show(t))
  
  implicit def mountR(base: String) = new {
    def /(f: (Request => Action[String] => Option[Response])) = resource(base, f)
  }
  
  implicit def showMe[T](t: T)(implicit r: Resourced[T]) = new {
    def rr = new {
      def show = r.show(t)

      def edit = r.edit(t)
    }
  }

  
  implicit val actionTraverse = new Traverse[rest.Action] {
    def traverse[F[_]: Applicative, A, B](f: A => F[B], t: Action[A]): F[Action[B]] = t match {
      case Index => (Index: Action[B]) η
      case Create => (Create: Action[B]) η
      case Show(v) => f(v) map { a => Show(a) }
      case Update(v) => f(v) map { a => Update(a) }
      case Destroy(v) => f(v) map { a => Destroy(a) }
      case New => (New: Action[B]) η
      case Edit(v) => f(v) map { a => Edit(a) }
    }
  }
  
  implicit def requestAction[IN[_]](request: Request) = new {
    lazy val action: Option[(String, Action[String])] = {
      request.underlying match {
        case MethodParts(GET, List(base)) => Some((base, rest.Index))
        case MethodParts(GET, List(base, "new")) => Some((base, New))
        case MethodParts(GET, List(base, id)) => Some((base, rest.Show(id)))
        case MethodParts(POST, List(base)) => Some((base, Create))
        case MethodParts(GET, List(base, id, "edit")) => Some((base, Edit(id)))
        case MethodParts(PUT, List(base, id)) => Some((base, Update(id)))
        case MethodParts(DELETE, List(base, id)) => Some((base, Destroy(id)))
        case _ => none
      }
    }
  }
}

package rest {

sealed trait Action[+T]

// Ze actions.
case object Index extends Action[Nothing]
case object Create extends Action[Nothing]
case class Show[T](id: T) extends Action[T]
case class Update[T](id: T) extends Action[T]
case class Destroy[T](id: T) extends Action[T]

// Ze actions for form requests
case object New extends Action[Nothing]
case class Edit[T](id: T) extends Action[T]

// Represents resource root, and action
// eg. Base("breweries", Index) could represent GET /breweries

// The parent for a nested resource
// given: /beers/5/hops
// this is: Context("beers","5") is the parent for Action("hops", Index)
case class Context(resource: String, id: String)

// A full request: any number of parent contexts and a final action
case class RestRequest[T](contexts: List[Context], resource: String, base: Action[T], contentType: Option[String])

case class Resource(name: String) {
  def show(id: String) = index + "/" + id

  def edit(id: String) = show(id) + "/edit"

  def index = "/" + name
}

trait Resourced[T] {
  val resource: Resource

  def id(t: T): String

  def show(t: T): String = resource.show(id(t))

  def edit(t: T): String = resource.edit(id(t))

  def index: String = resource.index
}
}
