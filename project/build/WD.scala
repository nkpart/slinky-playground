import sbt._

class WDProject(info: ProjectInfo) extends DefaultWebProject(info)
{
  val jetty6 = "org.mortbay.jetty" % "jetty" % "6.1.14" % "test"
  val servlet = "javax.servlet" % "servlet-api" % "2.5" % "provided"

  val derby = "org.apache.derby" % "derby" % "10.2.2.0" % "runtime"

  // Manually added to lib, need to get this working
  // val specs = "org.scala-tools.testing" %% "specs" % "1.6.1"

  // required because Ivy doesn't pull repositories from poms
  val smackRepo = "m2-repository-smack" at "http://maven.reucon.com/public"
}
