package barni.coralogix.service
import cats.Applicative
import cats.effect.{Resource, Sync}

import scala.{Console => SConsole}

trait Console[F[_]] {
  def putStringLn(input: String): F[Unit]
  def putString(input: String): F[Unit]
}
object Console {

  def apply[F[_]: Sync]: Console[F] = new Console[F] {
    override def putStringLn(input: String): F[Unit] = Sync[F].delay(SConsole.println(input))

    override def putString(input: String): F[Unit] = Sync[F].delay(SConsole.print(input))
  }

  def noop[F[_]](implicit F: Applicative[F]): Console[F] = new Console[F] {
    override def putStringLn(input: String): F[Unit] = F.unit

    override def putString(input: String): F[Unit] = F.unit
  }

  def resource[F[_]: Sync]: Resource[F, Console[F]] = Resource.pure(apply)

}
