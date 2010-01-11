package scapps

import scalaz.http.response._
import scalaz.http.request._
import scalaz.Scalaz._
import Scapps._

trait RestfulActions {
  def noAction(action: String) = {
    println("%s#%s: no implementation" format (getClass.getSimpleName, action))
    none
  }

  implicit val request: Request[Stream]

  def index: Option[Response[Stream]] = noAction("index")

  def create: Option[Response[Stream]] = noAction("create")

  def show(id: String): Option[Response[Stream]] = noAction("show")

  def update(id: String): Option[Response[Stream]] = noAction("update")

  def destroy(id: String): Option[Response[Stream]] = noAction("destroy")

  def nnew: Option[Response[Stream]] = noAction("nnew")

  def edit(id: String): Option[Response[Stream]] = noAction("edit")
}

object RestfulActions {
  import RichRequest._
  def mount(base: String, f: Request[Stream] => RestfulActions, idField: String = "id") = {
    reduce(List(
      path(base) >=> m(GET) >=> (r => f(r).index),
      path(base + "/new") >=> m(GET) >=> (r => f(r).nnew),
      path(base + "/:%s".format(idField)) >=> m(GET) >=> (r => f(r).show(r(idField).get)),
      path(base) >=> m(POST) >=> (r => f(r).create),
      path(base + "/:%s/edit".format(idField)) >=> m(GET) >=> (r => f(r).edit(r(idField).get)),
      path(base + "/:%s".format(idField)) >=> m(PUT) >=> (r => f(r).update(r(idField).get)),
      path(base + "/:%s".format(idField)) >=> m(DELETE) >=> (r => f(r).destroy(r(idField).get))
      ))
  }
}
