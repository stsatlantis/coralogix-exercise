package barni.coralogix.repository

import barni.coralogix.repository.Countable.syntax._
import barni.coralogix.repository.WordCountRepository.WordCount
import cats.effect.concurrent.Ref
import cats.effect.{Resource, Sync}
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.option._
import cats.{Applicative, Functor}

trait WordCountRepository[F[_], A] {
  def add(elem: A): F[Unit]
  def addMany(elems: A*)(implicit applicative: Applicative[F]): F[Unit] = elems.traverse_(add)
  def all(): F[List[WordCount]]
  def wipe(): F[Unit]
}

object WordCountRepository {

  type Key = (String, String)
  type Storage = Map[Key, Long]

  object Storage {
    val empty = Map.empty[Key, Long]
  }

  final case class WordCount(primaryKey: String, secondaryKey: String, count: Long)

  def apply[F[_]: Functor, A: Countable](storage: Ref[F, Storage]): WordCountRepository[F, A] =
    new WordCountRepository[F, A] {
      override def add(elem: A): F[Unit] =
        storage.update(_.updatedWith(elem.partitionKey -> elem.sortKey) {
          case Some(value) => (value + 1).some
          case None        => 1L.some
        })
      override def all(): F[List[WordCount]] =
        storage.get.map(_.toList.map { case ((primaryKey, secondaryKey), value) => WordCount(primaryKey, secondaryKey, value) })
      override def wipe(): F[Unit] = storage.set(Storage.empty)
    }

  def resource[F[_]: Sync, A: Countable](): Resource[F, WordCountRepository[F, A]] =
    Resource
      .liftF(Ref[F].of(Storage.empty))
      .map(apply[F, A])

}
