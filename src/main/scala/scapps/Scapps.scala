package scapps

import scalaz._
import scalaz.Scalaz._
import scalaz.http.request.Method
import belt._
import scapps.RichRequest._

trait RequestCreate[T] {
  def create(r: Request): Validation[NonEmptyList[(String,String)], T]
}

trait RequestUpdate[T] {
  def update(r: Request)(t: T): (List[(String, String)], T)
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

  def whenR(f: Request => Boolean) = when[Request](f)

  def check[M[_], A, B, C](k: Kleisli[M, A, B], fail: Kleisli[M, A, C])(success: Kleisli[M, B, C])(implicit fr: FoldRight[M], b: Bind[M]): Kleisli[M, A, C] = ☆((a: A) => {
    val v = k(a)
    if (v.empty) fail(a) else v >>= (x => success(x))
  })

  def reduce[T](rs: List[Kleisli[Option, Request, T]]): Kleisli[Option, Request, T] = {
    val first = rs.map(k => (k apply _) ∘ (_.fst)).∑
    kleisli(r => first(r).value)
  }

  def m(method: Method) = whenR(_.method == method)

  def at(parts: List[String]) = whenR(_.parts == parts)

  def at(s: String*) = whenR(_.parts == s.toList)
}