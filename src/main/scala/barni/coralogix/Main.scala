package barni.coralogix

import barni.coralogix.app.App
import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    App.run[IO](args).as(ExitCode.Success)
}
