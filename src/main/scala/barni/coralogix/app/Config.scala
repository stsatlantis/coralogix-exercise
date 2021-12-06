package barni.coralogix.app

import barni.coralogix.service.{HttpServer, WordCountService}
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

final case class Config(
  wordCount: WordCountService.Config,
  server: HttpServer.Config
)

object Config {

  implicit val wordCountConfigRead: ConfigReader[WordCountService.Config] = deriveReader
  implicit val serverConfigRead: ConfigReader[HttpServer.Config] = deriveReader
  implicit val configReader: ConfigReader[Config] = deriveReader

}
