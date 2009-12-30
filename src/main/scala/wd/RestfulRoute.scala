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
   
  def mount(base: String) = List(
    path(base) >=> m(GET) >=> index _,
    path(base + "/:id") >=> m(GET) >=> show _,
    path(base + "/new") >=> m(GET) >=> nnew _,
    path(base) >=> m(POST) >=> create _,
    path(base + "/:id/edit") >=> m(GET) >=> edit _,
    path(base + "/:id") >=> m(PUT) >=> update _,
    path(base + "/:id") >=> m(DELETE) >=> destroy _
  ).∑
  
  def mountByName = {
    val d = getClass.getSimpleName.stripSuffix("Controller").toLowerCase
    mount(d)
  }
}

trait DefaultRestfulRoute extends RestfulRoute {
  def index(request: Request[Stream]): Option[Response[Stream]] = none
  def create(request: Request[Stream]): Option[Response[Stream]] = none
  def show(request: Request[Stream]): Option[Response[Stream]] = none
  def update(request: Request[Stream]): Option[Response[Stream]] = none
  def destroy(request: Request[Stream]): Option[Response[Stream]] = none
  
  def nnew(request: Request[Stream]): Option[Response[Stream]] = none
  def edit(request: Request[Stream]): Option[Response[Stream]] = none
}