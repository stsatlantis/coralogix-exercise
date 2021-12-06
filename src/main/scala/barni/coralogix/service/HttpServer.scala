package barni.coralogix.service

import barni.coralogix.api.WordCountApi
import cats.effect.{Blocker, ConcurrentEffect, ExitCode, Timer}
import fs2.Stream
import org.http4s.server.blaze.BlazeServerBuilder

trait HttpServer[F[_]] {
  def start: Stream[F, ExitCode]
}

object HttpServer {

  final case class Config(port: Int)

  def server[F[_]: ConcurrentEffect: Timer, A](
    blocker: Blocker
  )(config: Config, wordCountApi: WordCountApi[F]): HttpServer[F] =
    new HttpServer[F] {
      import org.http4s.implicits._
      val interface = "0.0.0.0"

      override def start: Stream[F, ExitCode] =
        BlazeServerBuilder[F](blocker.blockingContext)
          .bindHttp(config.port, interface)
          .withHttpApp(wordCountApi.getCurrentCount.orNotFound)
          .serve
    }

}
