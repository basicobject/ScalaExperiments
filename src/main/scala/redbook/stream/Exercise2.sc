sealed trait Stream[+A] {
  def headOption: Option[A]
  def take(n: Int): Stream[A]
  def drop(n: Int): Stream[A]
  def toList: List[A]
  def takeWhile(p: A => Boolean): Stream[A]
  def exists(p: A => Boolean): Boolean
  def foldRight[B](z: => B)(f: (A, => B) => B): B
  def forAll(p: A => Boolean): Boolean
  def map[B](f: A => B): Stream[B]
  def filter(f: A => Boolean): Stream[A]
  def append[B >: A](b: => Stream[B]): Stream[B]
  def flatMap[B](f: A => Stream[B]): Stream[B]
  def find(p: A => Boolean): Option[A]
}

object Stream {
  def empty[A]: Stream[A] = Empty
  def cons[A](head: => A, tail: => Stream[A]): Stream[A] = {
    lazy val h = head
    lazy val t = tail
    Cons(() => h, () => t)
  }

  def apply[A](xs: A*): Stream[A] = {
    if (xs.isEmpty) Empty else cons(xs.head, apply(xs.tail: _*))
  }
}

case object Empty extends Stream[Nothing] {
  override def take(n: Int): Stream[Nothing] = Empty

  override def drop(n: Int): Stream[Nothing] = Empty

  override def toList: List[Nothing] = List.empty

  override def takeWhile(p: Nothing => Boolean) = Empty

  override def exists(p: Nothing => Boolean) = false

  override def foldRight[B](z: => B)(f: (Nothing, => B) => B): B = z

  override def headOption = None

  override def forAll(p: Nothing => Boolean): Boolean = false

  override def map[B](f: Nothing => B) = Stream.empty[B]

  override def filter(f: Nothing => Boolean): Stream[Nothing] = Stream.empty

  override def append[B >: Nothing](b: => Stream[B]): Stream[B] = b

  override def flatMap[B](f: Nothing => Stream[B]): Stream[B] = Stream.empty[B]

  override def find(p: Nothing => Boolean): Option[Nothing] = None
}

case class Cons[+A](head: () => A, tail: () => Stream[A]) extends Stream[A] {
  override def take(n: Int): Stream[A] = {
    if (n > 0) Stream.cons(this.head(), this.tail().take(n - 1))
    else Empty
  }

  override def drop(n: Int): Stream[A] = {
    if (n > 0) tail().drop(n-1)
    else this
  }

  override def toList: List[A] = List(head()) ++ tail().toList

  override def takeWhile(p: A => Boolean): Stream[A] = if(p(head()))
    Stream.cons(head(), tail().takeWhile(p))
  else Stream.empty

  override def exists(p: A => Boolean): Boolean = p(head()) || tail().exists(p)

  override def foldRight[B](z: => B)(f: (A, => B) => B): B = f(head(), tail().foldRight(z)(f))

  override def headOption = Some(head())

  override def forAll(p: A => Boolean): Boolean = foldRight(true)((a, b) => p(a) && b)

  override def map[B](f: A => B): Stream[B] = Stream.cons(f(head()), tail().map(f))

  override def filter(f: A => Boolean): Stream[A] = if(f(head())) Stream.cons(head(), tail().filter(f)) else tail().filter(f)

  override def append[B >: A](b: => Stream[B]): Stream[B] = Stream.cons(head(), tail().append(b))

  override def flatMap[B](f: A => Stream[B]): Stream[B] = f(head()).append(tail().flatMap(f))

  override def find(p: A => Boolean): Option[A] = filter(p).headOption
}

val s = Stream(1,2,3,4,5)

s.take(2).toList
s.drop(2).toList
s.takeWhile(_ % 2 == 1).toList


s.exists(_%5  == 0)

// exists using foldRight
def exists[A](stream: Stream[A])(p: A => Boolean): Boolean = stream.foldRight(false)((a, _) => p(a))

s.forAll(_ % 2 == 1)
s.forAll(_ > 0)
s.forAll(_ < 0)
val s2 = Stream(1,1,1,1,2)
s2.forAll(_ == 1)

// takeWhile using foldRight

def takeWhile[A](stream: Stream[A])(p: A => Boolean): Stream[A] =
  stream.foldRight(Stream.empty[A])((a, b) => if (p(a)) Stream.cons(a, b) else Stream.empty)

takeWhile(s)(_ % 2 == 1).toList

// headOption using foldRight

def headOption[A](stream: Stream[A]): Option[A] = stream.foldRight(Option.empty[A])((a, _) => Some(a))

headOption(s)
headOption(Stream.empty)

s.map(_* 2).toList

s.filter(_%2 ==1).toList

s.append(Stream(10)).toList

Stream(2,3).flatMap((x) => Stream(x%2, x%3)).toList

// map using foldRight

def map[A, B](stream: Stream[A])(f: A => B): Stream[B] = stream.foldRight(Stream.empty[B])((a, b) => Stream.cons(f(a), b))

map(s)(_ * 2).toList

// filter using foldRight

def filter[A](stream: Stream[A])(p: A => Boolean): Stream[A] = stream.foldRight(Stream.empty[A])((a, b) => if(p(a)) Stream.cons(a, b) else b)

filter(s)(_ % 2 == 0).toList

// append using foldRight
def append[A](stream1: Stream[A], stream2: => Stream[A]): Stream[A] = stream1.foldRight(stream2)((a, b) => Stream.cons(a, b))

append(Stream(1,2,3), Stream(4,5)).toList

// flatMap using foldRight

def flatMap[A, B](stream: Stream[A])(f: A => Stream[B]): Stream[B] = stream.foldRight(Stream.empty[B])((a, b) => f(a).append(b))
flatMap(Stream(2,3))((x) => Stream(x%2, x%3)).toList


Stream(1,2,3,4).map(_ + 10).filter(_ % 2 == 0)

val ones: Stream[Int] = Stream.cons(1, ones)

ones.take(5).toList

ones.exists(_%2==1)

ones.map(_ + 1).exists(_ %2 == 0)

