package barni.coralogix.repository

import barni.coralogix.service.Console
import cats.effect.{ConcurrentEffect, Resource}
import cats.syntax.bifunctor._
import cats.syntax.eq._
import fs2.Stream
import fs2.text.utf8DecodeC
import io.circe.Decoder
import io.circe.parser._

trait WordStream[F[_], A] {
  def stream: Stream[F, A]
}

object WordStream {

  def resource[F[_], A: Decoder](
    console: Console[F]
  )(commands: List[String])(implicit F: ConcurrentEffect[F]): Resource[F, WordStream[F, A]] = {
    eu.monniot.process.Process.spawn[F](commands).map { process =>
      new WordStream[F, A] {
        override def stream: Stream[F, A] =
          process.stdout
            .split(_ === '\n')
            .through(utf8DecodeC[F])
            .map(input => decode[A](input).leftMap(_ => input))
            .evalTap { _.fold(e => console.putStringLn(s"Unable to decode input: $e"), _ => F.unit) }
            .collect { case Right(value) => value }
      }
    }
  }
}
