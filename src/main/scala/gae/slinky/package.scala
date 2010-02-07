package gae

import scalaz._
import Scalaz._
import belt._

// Useful functions for writing slinky apps on GAE
package object slinky {
  type Filter = Kleisli[Option, Request, Request]
  
  import Services._
  
  val isLoggedIn: Filter = ☆(userService.isUserLoggedIn.option(_:Request))
  
  val isAdmin: Filter =  ☆((r:Request) => userService.isUserAdmin.option(r) )
  
  def doLogin(returnTo: String = "/") = ((r: Request) => {
    belt.redirect(userService.createLoginURL(returnTo))
  }).kleisli[Option]
  
  def adminOnly(f: Request => Response, denied: => Response): Request => Response = r => {
    userService.isUserAdmin ? f(r) | denied
  }
  
  def authorised[T](t: => T): Option[T] = {
    userService.isUserAdmin ? some(t) | none
  }
}