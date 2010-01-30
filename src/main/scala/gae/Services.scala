package gae

import com.google.appengine.api.users._
import com.google.appengine.api.datastore._

import scala.util.DynamicVariable

object Services {
  private val dynUserService: DynamicVariable[(UserService, DatastoreService)] = new DynamicVariable(null)
  
  implicit def userService =      dynUserService.value._1
  implicit def datastoreService = dynUserService.value._2
  
  def service[T](t: => T): T = {
    val ds = DatastoreServiceFactory.getDatastoreService
    val us = UserServiceFactory.getUserService
    
    dynUserService.withValue((us, ds)) {
      t
    }
  }
  
}