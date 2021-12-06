package barni.coralogix.app

import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Sync, Timer}
import fs2.Stream
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._
object App {

  private[this] def loadConfig[F[_]: Sync: ContextShift](blocker: Blocker): F[Config] =
    ConfigSource.defaultApplication.at("app").loadF(blocker)

  def run[F[_]: ConcurrentEffect: ContextShift: Timer](command: List[String]): F[Unit] =
    Blocker[F]
      .flatMap { blocker =>
        Resource
          .liftF(loadConfig(blocker))
          .flatMap(Resources.resource[F](blocker)(_, command))
      }
      .use { resources =>
        Stream(resources.services.wordCountService.process().drain, resources.services.http.start.drain).parJoinUnbounded.compile.drain
      }

}
