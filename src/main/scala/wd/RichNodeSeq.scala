package wd

import xml._
import xml.transform._

trait RichNodeSeq {
  val ns: NodeSeq

  def mapTree(f: Node => NodeSeq): NodeSeq = {
    new RuleTransformer(new RewriteRule {
      override def transform(n: Node) = f(n)
    }).transform(ns)
  }

  def replaceAll(n: NodeSeq, f: NodeSeq) = mapTree(_ match {
    case e if e == n => f
    case e => e
  })
}
