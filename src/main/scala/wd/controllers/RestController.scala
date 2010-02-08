package wd
package controllers

import scalaz._
import Scalaz._
import http.response.Forbidden
import Scalaz._
import xml.NodeSeq
import scapps._
import belt._
import gae._
import views.{errors, layouts}
import com.google.appengine.api.users.User

trait WDLayout extends Layout {
  implicit val charset = UTF8

  def layout(ns: NodeSeq) = layouts.main(gae.Services.userService)(ns)

  def us = gae.Services.userService

  def authenticated(f: User => Response): Response = {
    us.currentUser ∘ f | render(errors._403_, Forbidden)
  }

  def authenticated(response: => Response): Response = {
    authenticated(_ => response)
  }

  def adminOnly[T](f: User => T): Validation[Response, T] = {
    val done = us.currentUser.filter(_ => us.isUserAdmin) ∘ f
    done.toSuccess(render(errors._403_, Forbidden))
  }

  def adminOnly[T](t: => T): Validation[Response, T] = {
    adminOnly(_ => t)
  }
}

trait RestController[T] extends WDLayout {
  def apply(v: rest.Action[T]): Option[Response]
}

