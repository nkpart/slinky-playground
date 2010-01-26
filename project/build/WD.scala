import sbt._

class WDProject(info: ProjectInfo) extends DefaultWebProject(info) {
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.14" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"

  val junit = "junit" % "junit" % "4.5" % "test"
  
  val derby = "org.apache.derby" % "derby" % "10.2.2.0" % "runtime"

  val specs = "org.scala-tools.testing" %% "specs" % "1.6.2" % "test"
  
  val inflector = "prohax" %% "inflector" % "1.0"

  // required because Ivy doesn't pull repositories from poms
  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
  val r2 = "nexus something" at "http://nexus.scala-tools.org/content/repositories/releases"

  val r3 = "something" at "http://scala-tools.org/repo-snapshots"  
  val scalaz_core = "com.googlecode.scalaz" % "scalaz-core_2.8.0.Beta1-RC8" % "5.0-SNAPSHOT"
  val scalaz_http = "com.googlecode.scalaz" % "scalaz-http_2.8.0.Beta1-RC8" % "5.0-SNAPSHOT"
}
