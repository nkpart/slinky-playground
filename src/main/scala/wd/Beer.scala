package wd

import scalaz._
import http.request.Request
import Scalaz._

import scala.collection.JavaConversions.asMap
import com.google.appengine.api.datastore.{DatastoreService, Key, Entity}
import gae._
import scapps.{RequestUpdate, RequestCreate}

case class Style(value: String) extends NewType[String]

case class Beer(name: String, style: Style)

