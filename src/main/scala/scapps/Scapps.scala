package scapps

import scalaz._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.response._

trait Postable[T] {
  def create[IN[_] : FoldLeft](r: Request[IN]): Validation[List[(String,String)], T]

  def update[IN[_] : FoldLeft](r: Request[IN])(t: T): (List[(String,String)], T)
}

object Scapps {
  def methodHax[IN[_]: FoldLeft]: (Request[IN] => Request[IN]) = ((r: Request[IN]) =>
    ((r !| "_method") ∘ (_.mkString) >>= (scalaz.http.Slinky.StringMethod _)) ∘ { (method:Method) => r(method) } getOrElse r
  )

  def addParam[IN[_]](request: Request[IN], key: String, value: String) = {
    val kv = (key + "=" + value).toList // TODO escape key and value
    val newUri = request.line.uri ++++ {
      _ match {
        case Some(qs) => Some(qs ++ ('&' :: kv))
        case None => Some(kv)
      }
    }
    request(request.line(newUri))
  }

  def when[T](f: T => Boolean): Kleisli[Option, T, T] = ☆((t: T) => if (f(t)) some(t) else none)

  def whenR(f: Request[Stream] => Boolean) = when[Request[Stream]](f)

  def check[M[_], A, B, C](k: Kleisli[M, A, B], fail: Kleisli[M, A, C])(success: Kleisli[M, B, C])(implicit fr: FoldRight[M], b: Bind[M]): Kleisli[M, A, C] = ☆((a: A) => {
    val v = k(a)
    if (v.empty) fail(a) else v >>= (success apply _)
    //    k(a) some { x => success(x) } none { fail(a) }
  })

  def reduce[T](rs: List[Kleisli[Option, Request[Stream], T]]): Kleisli[Option, Request[Stream], T] = {
    val first = rs.map(k => (k apply _) ∘ (_.fst)).∑
    kleisli(r => first(r).value)
  }

  def m(method: Method) = whenR(_.method == method)

  def at(parts: List[String]) = whenR(_.parts == parts)

  def at(s: String*) = whenR(_.parts == s.toList)

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
            case Some(Some((k, v))) => addParam(r, k, v)
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