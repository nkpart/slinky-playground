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
