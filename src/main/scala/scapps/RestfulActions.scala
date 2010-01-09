package scapps

import scalaz.http.response._
import scalaz.http.request._

trait RestfulActions {
  def noAction(action: String) = {
    println("%s#%s: no implementation" format (getClass.getSimpleName, action))
    none
  }

  implicit val request: Request[Stream]

  def index: Option[Response[Stream]] = noAction("index")

  def create: Option[Response[Stream]] = noAction("create")

  def show: Option[Response[Stream]] = noAction("show")

  def update: Option[Response[Stream]] = noAction("update")

  def destroy: Option[Response[Stream]] = noAction("destroy")

  def nnew: Option[Response[Stream]] = noAction("nnew")

  def edit: Option[Response[Stream]] = noAction("edit")
}

object RestfulActions {
  def mount(base: String, f: Request[Stream] => RestfulActions) = {
    reduce(List(
      path(base) >=> m(GET) >=> (r => f(r).index),
      path(base + "/new") >=> m(GET) >=> (r => f(r).nnew),
      path(base + "/:id") >=> m(GET) >=> (r => f(r).show),
      path(base) >=> m(POST) >=> (r => f(r).create),
      path(base + "/:id/edit") >=> m(GET) >=> (r => {
        println("here")
        println(r.uri.queryString)
        f(r).edit
      }),
      path(base + "/:id") >=> m(PUT) >=> (r => f(r).update),
      path(base + "/:id") >=> m(DELETE) >=> (r => f(r).destroy)
      ))
  }
}
