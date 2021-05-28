sealed trait List[+A]

object List {
  def apply[A](xs: A*): List[A] =
    if (xs.isEmpty) Nil else Cons(xs.head, apply(xs.tail: _*))
}

object Nil extends List[Nothing]
case class Cons[+A](head: A, tail: List[A]) extends List[A]


def map[A,B](list: List[A])(f: A => B): List[B] = list match {
  case Nil => Nil
  case Cons(x, xs) => Cons(f(x), map(xs)(f))
}

val a = List(1,2,3,4)
map(a)(_ * 2)
map(a)(_.toString)
