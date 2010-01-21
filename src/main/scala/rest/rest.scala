import rest.Resourced
import scalaz._
import Scalaz._

package object rest {
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
    
    // override def fmap[A,B](a: Action[A], f: A => B): Action[B] = a match {
    //   case Index => Index
    //   case Create => Create
    //   case Show(v) => Show(f(v))
    //   case Update(v) => Update(f(v))
    //   case Destroy(v) => Destroy(f(v))
    //   case New => New
    //   case Edit(v) => Edit(f(v))
    // }
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
  def show(id: String) = "/" + name + "/" + id

  def edit(id: String) = show(id) + "/edit"

  def index = name
}

trait Resourced[T] {
  val resource: Resource

  def id(t: T): String

  def show(t: T): String = resource.show(id(t))

  def edit(t: T): String = resource.edit(id(t))
}
}
