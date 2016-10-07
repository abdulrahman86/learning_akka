package chapter3.tell

import scala.collection.mutable.StringBuilder

object Leet {

  trait Leetable[T] {
    def toLeet(in: T): String

    def toLeet2(in: T): String
  }

  def apply[T](in: T)(implicit leetable: Leetable[T]): String = {
    assert(leetable.toLeet(in) == leetable.toLeet2(in))
    leetable.toLeet2(in)
  }
}

object Main {
  def main(args: Array[String]): Unit = {

    import Leet._

    implicit object StringLeetable extends Leetable[String] {
      def toLeet(in: String): String = in.toList.foldLeft[StringBuilder](new StringBuilder)((x, y) => {
        y match {
          case 'A' | 'a' => x append '4'
          case 'E' | 'e' => x append '3'
          case 'i' | 'I' => x append '1'
          case 'o' | 'O' => x append '0'
          case 's' | 'S' => x append '5'
          case 't' | 'T' => x append '7'
          case y => x append y
        }
      }) toString

      def toLeet2(in: String): String = in.reverse.toList.foldLeft[List[Char]](List())((x, y) => {
        y match {
          case 'A' | 'a' => '4' :: x
          case 'E' | 'e' => '3' :: x
          case 'i' | 'I' => '1' :: x
          case 'o' | 'O' => '0' :: x
          case 's' | 'S' => '5' :: x
          case 't' | 'T' => '7' :: x
          case y => y :: x
        }
      }) mkString
    }

    assert(Leet("Let's have some fun.") == "L37'5 h4v3 50m3 fun.")
    assert(Leet("C is for cookie, that's good enough for me") == "C 15 f0r c00k13, 7h47'5 g00d 3n0ugh f0r m3")
    assert(Leet("By the power of Grayskull!") == "By 7h3 p0w3r 0f Gr4y5kull!")

    println(Leet(args(0)))
  }
}


