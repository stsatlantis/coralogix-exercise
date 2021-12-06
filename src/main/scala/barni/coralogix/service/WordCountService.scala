package barni.coralogix.service

import barni.coralogix.repository.{WordCountRepository, WordStream}
import cats.effect.{Concurrent, Resource, Timer}
import fs2.Stream

import scala.concurrent.duration.FiniteDuration

trait WordCountService[F[_]] {

  def process(): Stream[F, Unit]

}

object WordCountService {

  final case class Config(size: Int, window: FiniteDuration, timeout: FiniteDuration)

  def apply[F[_]: Concurrent: Timer, A](config: Config,
                                        source: WordStream[F, A],
                                        wordCountRepository: WordCountRepository[F, A]): WordCountService[F] =
    new WordCountService[F] {

      override def process(): Stream[F, Unit] = {
        val resetStream: Stream[F, Unit] = Stream
          .repeatEval(wordCountRepository.wipe())
          .metered(config.timeout)

        val processingStream = source.stream
          .groupWithin(config.size, config.window)
          .evalTap(chunks => wordCountRepository.addMany(chunks.toList: _*))
          .map(_ => ())

        resetStream.concurrently(processingStream)

      }
    }

  def resource[F[_]: Concurrent: Timer, A](config: Config,
                                           source: WordStream[F, A],
                                           wordCountRepository: WordCountRepository[F, A]): Resource[F, WordCountService[F]] =
    Resource.pure(apply(config, source, wordCountRepository))

}
