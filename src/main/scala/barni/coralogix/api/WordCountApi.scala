package barni.coralogix.api

import barni.coralogix.api.ApiDescription.EventCountResponse
import barni.coralogix.repository.WordCountRepository
import barni.coralogix.repository.WordCountRepository.WordCount
import cats.effect.{ContextShift, Resource, Sync}
import cats.syntax.either._
import cats.syntax.functor._
import org.http4s.HttpRoutes

trait WordCountApi[F[_]] {
  def getCurrentCount: HttpRoutes[F]
}

object WordCountApi {

  def apply[F[_]: Sync: ContextShift, A](repo: WordCountRepository[F, A]): WordCountApi[F] = new WordCountApi[F] {
    import sttp.tapir.server.http4s._

    override def getCurrentCount: HttpRoutes[F] =
      ApiDescription.getCurrentCount.serverLogic { _ =>
        repo
          .all()
          .map {
            _.groupBy(_.primaryKey).map {
              case (eventType, words) =>
                EventCountResponse.EventCount(eventType, words.map {
                  case WordCount(_, secondaryKey, count) => EventCountResponse.WordCount(secondaryKey, count)
                })
            }.toList
          }
          .map(e => EventCountResponse(e).asRight[Unit])
      }.toRoutes
  }

  def resource[F[_]: Sync: ContextShift, A](repo: WordCountRepository[F, A]): Resource[F, WordCountApi[F]] =
    Resource.pure(apply(repo))

}
