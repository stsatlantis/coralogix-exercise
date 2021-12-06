package barni.coralogix.app

import barni.coralogix.api.WordCountApi
import barni.coralogix.app.Resources._
import barni.coralogix.model.Event
import barni.coralogix.repository.{WordCountRepository, WordStream}
import barni.coralogix.service.{Console, HttpServer, WordCountService}
import cats.effect._
import cats.syntax.apply._

case class Resources[F[_]](
  system: System[F],
  storage: Storage[F],
  services: Services[F]
)

object Resources {

  final case class System[F[_]](
    console: Console[F]
  )

  final case class Storage[F[_]](
    wordCount: WordCountRepository[F, Event],
    words: WordStream[F, Event]
  )

  final case class Services[F[_]](
    http: HttpServer[F],
    wordCountService: WordCountService[F]
  )

  def system[F[_]: Sync]: Resource[F, System[F]] = Console.resource.map(System(_))

  def storage[F[_]: ConcurrentEffect](console: Console[F], command: List[String]): Resource[F, Storage[F]] = {
    (WordCountRepository.resource[F, Event](), WordStream.resource[F, Event](console)(command)).mapN(Storage.apply)
  }

  def service[F[_]: ConcurrentEffect: ContextShift: Timer](
    blocker: Blocker
  )(serverConfig: HttpServer.Config,
    serviceConfig: WordCountService.Config,
    repository: WordCountRepository[F, Event],
    wordStream: WordStream[F, Event]): Resource[F, Services[F]] = {
    (WordCountApi
       .resource[F, Event](repository)
       .map(HttpServer.server(blocker)(serverConfig, _)),
     WordCountService.resource[F, Event](serviceConfig, wordStream, repository)).mapN(Services.apply)
  }

  def resource[F[_]: ConcurrentEffect: ContextShift: Timer](blocker: Blocker)(appConfig: Config,
                                                                              args: List[String]): Resource[F, Resources[F]] =
    for {
      sys <- system[F]
      storage <- storage(sys.console, args)
      services <- service(blocker)(appConfig.server, appConfig.wordCount, storage.wordCount, storage.words)
    } yield Resources[F](sys, storage, services)

}
