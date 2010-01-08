package wd

import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.response._
import Scapps._

trait RestfulRoute {
  def index(request: Request[Stream]): Option[Response[Stream]]

  def create(request: Request[Stream]): Option[Response[Stream]]

  def show(request: Request[Stream]): Option[Response[Stream]]

  def update(request: Request[Stream]): Option[Response[Stream]]

  def destroy(request: Request[Stream]): Option[Response[Stream]]

  def nnew(request: Request[Stream]): Option[Response[Stream]]

  def edit(request: Request[Stream]): Option[Response[Stream]]

  def mount(base: String) = reduce(List(
    path(base) >=> m(GET) >=> index _,
    path(base + "/new") >=> m(GET) >=> nnew _,
    path(base + "/:id") >=> m(GET) >=> show _,
    path(base) >=> m(POST) >=> create _,
    path(base + "/:id/edit") >=> m(GET) >=> edit _,
    path(base + "/:id") >=> m(PUT) >=> update _,
    path(base + "/:id") >=> m(DELETE) >=> destroy _
    ))

  def mountByName = {
    val d = getClass.getSimpleName.stripSuffix("Controller").toLowerCase
    mount(d)
  }
}

trait DefaultRestfulRoute extends RestfulRoute {
  def noAction(action: String) = {
    val nm = getClass.getSimpleName
    println("%s: No action %s" format (nm, action))
    none
  }

  def index(request: Request[Stream]): Option[Response[Stream]] = noAction("index")

  def create(request: Request[Stream]): Option[Response[Stream]] = noAction("create")

  def show(request: Request[Stream]): Option[Response[Stream]] = noAction("show")

  def update(request: Request[Stream]): Option[Response[Stream]] = noAction("update")

  def destroy(request: Request[Stream]): Option[Response[Stream]] = noAction("destroy")

  def nnew(request: Request[Stream]): Option[Response[Stream]] = noAction("nnew")

  def edit(request: Request[Stream]): Option[Response[Stream]] = noAction("edit")
}