package scapps

import scalaz._
import http.InputStreamer
import http.request._
import http.response._
import http.servlet.HttpServletRequest._
import http.servlet.HttpServletResponse._
import http.servlet.{HttpServlet, HttpServletRequest}
import Scalaz._

/**
 * A servlet that is a scalaz.http.servlet.ServletApplication. It is intended that subclasses apply one or more type arguments until a
 * concrete class is had.
 *
 * TODO: Make ServletApplication a trait and mix that in, in place of the handle method
 */
abstract class ServletApplicationServlet[IN[_], OUT[_]](implicit in: InputStreamer[IN], e: Each[OUT]) extends javax.servlet.http.HttpServlet {
  /**
   * Applies the given request to the underlying web application and sends the resulting response to the given response.
   */
  override final def service(request: javax.servlet.http.HttpServletRequest, response: javax.servlet.http.HttpServletResponse) {
    request.asRequest[IN].foreach(r => {
      val res = apply(this, request, r)
      response.respond[OUT](res)
    })
  }

  def apply(implicit servlet: HttpServlet, servletRequest: HttpServletRequest, request: Request[IN]): Response[OUT]
}

/**
 * A servlet that is a scalaz.http.Application. It is intended that subclasses apply one or more type arguments until a
 * concrete class is had.
 *
 * TODO: Make ServletApplication a trait and mix that in, in place of the handle method
 */
abstract class ApplicationServlet[IN[_], OUT[_]](implicit in: InputStreamer[IN], e: Each[OUT]) extends javax.servlet.http.HttpServlet {
  /**
   * Applies the given request to the underlying web application and sends the resulting response to the given response.
   */
  override final def service(request: javax.servlet.http.HttpServletRequest, response: javax.servlet.http.HttpServletResponse) {
    request.asRequest[IN].foreach(r => {
      val res = apply(r)
      response.respond[OUT](res)
    })
  }

  def apply(implicit req : Request[IN]) : Response[OUT]
}
