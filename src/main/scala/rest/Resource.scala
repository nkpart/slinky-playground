import rest.Resourced

package object rest {
  implicit def showMe[T](t: T)(implicit r: Resourced[T]) = new {
    def rr = new {
      def show = r.show(t)

      def edit = r.edit(t)
    }
  }
}

package rest {

sealed trait Action

// Ze actions.
case object Index extends Action
case object Create extends Action
case class Show(id: String) extends Action
case class Update(id: String) extends Action
case class Destroy(id: String) extends Action

// Ze actions for form requests
case object New extends Action
case class Edit(id: String) extends Action

// Represents resource root, and action
// eg. Base("breweries", Index) could represent GET /breweries

// The parent for a nested resource
// given: /beers/5/hops
// this is: Context("beers","5") is the parent for Action("hops", Index)
case class Context(resource: String, id: String)

// A full request: any number of parent contexts and a final action
case class RestRequest(contexts: List[Context], resource: String, base: Action, contentType: Option[String])

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
