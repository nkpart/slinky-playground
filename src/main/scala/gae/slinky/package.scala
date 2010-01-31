package gae

import scalaz._
import Scalaz._
import http.request.Request
import http.response._

// Useful functions for writing slinky apps on GAE
package object slinky {
  type Filter = Kleisli[Option, Request[Stream], Request[Stream]]
  
  import Services._
  
  val isLoggedIn: Filter = ☆(userService.isUserLoggedIn.option(_:Request[Stream]))
  
  val isAdmin: Filter =  ☆((r:Request[Stream]) => userService.isUserAdmin.option(r) )
  
  def doLogin(returnTo: String = "/") = ((r: Request[Stream]) => {   
    implicit val re = r
    Response.redirects[Stream, Stream](userService.createLoginURL(returnTo))
  }).kleisli[Option]
}