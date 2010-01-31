package scapps

import scalaz._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.response._
import scapps.RichRequest._

trait RequestCreate[T] {
  def create[IN[_]: FoldLeft](r: Request[IN]): Validation[NonEmptyList[(String,String)], T]
}

trait RequestUpdate[T] {
  def update[IN[_]: FoldLeft](r: Request[IN])(t: T): (List[(String, String)], T)
}

object Scapps {
  type Around[T] = (=> T) => T
  
  implicit def AroundMonoid[T] = new scalaz.Semigroup[Around[T]] {
    def append(s1: Around[T], s2: => Around[T]): Around[T] = t => s1(s2(t))
  }
  
  implicit def AroundZero[T] = new scalaz.Zero[Around[T]] {
    def help(t: => T): T = t
    val zero = help _
  }
  
  import RichRequest._

  def when[T](f: T => Boolean): Kleisli[Option, T, T] = ☆((t: T) => if (f(t)) some(t) else none)

  def whenR(f: Request[Stream] => Boolean) = when[Request[Stream]](f)

  def check[M[_], A, B, C](k: Kleisli[M, A, B], fail: Kleisli[M, A, C])(success: Kleisli[M, B, C])(implicit fr: FoldRight[M], b: Bind[M]): Kleisli[M, A, C] = ☆((a: A) => {
    val v = k(a)
    if (v.empty) fail(a) else v >>= (x => success(x))
  })

  def reduce[T](rs: List[Kleisli[Option, Request[Stream], T]]): Kleisli[Option, Request[Stream], T] = {
    val first = rs.map(k => (k apply _) ∘ (_.fst)).∑
    kleisli(r => first(r).value)
  }

  def m(method: Method) = whenR(_.method == method)

  def at(parts: List[String]) = whenR(_.parts == parts)

  def at(s: String*) = whenR(_.parts == s.toList)

  // Matches paths of this form:
  //   /a/:b/c
  // Any part preceded by a : will be written into the query string.
  //   ie. If constructed with /a/:b/c and hit with /a/5/c, (request ! "b") will be "5"
  def path(s: String) = {
    val parts = s.stripPrefix("/").stripSuffix("/").split("/")

    ☆((request: Request[Stream]) =>
      if (request.parts.size == parts.size) {
        val checks = parts.toList zip request.parts map {
          case (a, b) =>
            if (a.startsWith(":")) {
              val k = a.stripPrefix(":")
              some(some((k, b)))
            } else {
              (a == b).option(none)
            }
        }
        if (checks ∀ (_.isDefined)) {
          some(checks.foldl(request, (r: Request[Stream], check: Option[Option[(String, String)]]) => check match {
            case Some(Some((k, v))) => r.addQueryParam(k, v)
            case Some(None) => r
            case None => r // should never reach here, all should be defined
          }))
        } else {
          none
        }
      } else {
        none
      })
  }
}