package barni.coralogix.repository

trait Countable[A] {
  def partitionKey(input: A): String
  def sortKey(input: A): String
}

object Countable {

  implicit def apply[A](implicit instance: Countable[A]): Countable[A] = instance

  object syntax {

    implicit final class CountableOps[A](private val input: A) extends AnyVal {
      def partitionKey(implicit instance: Countable[A]): String = instance.partitionKey(input)
      def sortKey(implicit instance: Countable[A]): String = instance.sortKey(input)
    }

  }

}
