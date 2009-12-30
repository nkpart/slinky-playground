package wd

import scalaz._
import scalaz.Scalaz._
import scalaz.http.request._
import scalaz.http.response._

object Scapps {
  implicit def zeroKleisliZero[M[_],A,B](implicit z: Zero[M[B]]): Zero[Kleisli[M,A,B]] = new Zero[Kleisli[M,A,B]] {
    val zero : Kleisli[M,A,B] = {
      ☆((a : A) => ∅)
    }
  }

  implicit def semigroupKleisliSemigroup[M[_],A,B](implicit ss : Semigroup[M[B]]) : Semigroup[Kleisli[M,A,B]] = new Semigroup[Kleisli[M,A,B]] {
    def append(s1 : Kleisli[M,A,B], s2 : => Kleisli[M,A,B]) : Kleisli[M,A,B] = {
      ☆((a : A) => s1(a) ⊹ s2(a))
    }
  }

  implicit def KleisliFunctor[M[_],P](implicit ff : Functor[M]): Functor[PartialApplyKA[Kleisli,M,P]#Apply] = new Functor[PartialApplyKA[Kleisli,M,P]#Apply] {
    def fmap[A, B](k: Kleisli[M, P, A], f: A => B): Kleisli[M, P, B] = ☆((p : P) => ff.fmap(k(p), f))
  }

  implicit def KleisliMA[M[_], A, B](k: Kleisli[M,A,B]): MA[PartialApplyKA[Kleisli, M, A]#Apply, B] = ma[PartialApplyKA[Kleisli, M, A]#Apply, B](k)
  
    def addParam[IN[_]](request: Request[IN], key: String, value: String) = {
      val kv = (key + "=" + value).toList // TODO escape key and value
      val newUri = request.line.uri ++++ { _ match {
        case Some(qs) => Some(qs ++ ('&' :: kv))
        case None => Some(kv)
      }}
      request(request.line(newUri))
    }

  def when[T](f : T => Boolean) : Kleisli[Option,T,T] = ☆((t : T) => if (f(t)) some(t) else none)
  def whenR(f : Request[Stream] => Boolean) = when[Request[Stream]](f)

  def check[M[_], A, B, C](k : Kleisli[M, A, B], fail : Kleisli[M, A, C])(success : Kleisli[M, B, C])(implicit fr : FoldRight[M], b : Bind[M]) : Kleisli[M,A,C] =  ☆((a : A) => {
    val v = k(a)
    if (v.empty) fail(a) else v >>= (success apply _)
//    k(a) some { x => success(x) } none { fail(a) }
  })

  def m(method : Method) = whenR(_.method == method)
  def at(parts : List[String]) = whenR(_.parts == parts)
  def at(s : String*) = whenR(_.parts == s.toList)
  def path(s : String) = {
    val parts = s.stripPrefix("/").stripSuffix("/").split("/")

    ☆((request : Request[Stream]) => 
      if (request.parts.size == parts.size) {
        val checks = parts.toList zip request.parts map { case (a, b) => 
          if (a.startsWith(":")) {
            val k = a.stripPrefix(":")
            some(some((k, b)))
          } else {
            (a == b).option(none)
          }
         }
        if (checks ∀ (_.isDefined)) {
          some(checks.foldl(request, (r : Request[Stream], check : Option[Option[(String, String)]]) => check match {
            case Some(Some((k, v))) => addParam(request, k, v)
            case _ => request
           }))
        } else {
          none
        }
      } else {
        none
      }) 
  }
}