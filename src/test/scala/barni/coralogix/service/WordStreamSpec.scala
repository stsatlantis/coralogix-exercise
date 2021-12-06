package barni.coralogix.service

import barni.coralogix.UnitSpec
import barni.coralogix.model.Event
import barni.coralogix.repository.WordStream
import cats.effect.concurrent.Ref
import cats.effect.{ContextShift, IO, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.concurrent.ExecutionContext

class WordStreamSpec extends UnitSpec {

  "WordStream" should {

    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

    def testScriptPath = Thread.currentThread().getContextClassLoader.getResource("testscript").getPath
    "output only valid elements" in {
      WordStream
        .resource[IO, Event](Console.noop)(List(testScriptPath))
        .use(
          _.stream.compile.toList.map { events => events.size shouldBe 3 }
        )
        .unsafeRunSync()
    }

    "log invalid inputs" in {
      val console = Resource.liftF(Ref[IO].of(List.empty[String])).map { outputRef =>
        val console = new Console[IO] {
          override def putStringLn(input: String): IO[Unit] = outputRef.update(input +: _)

          override def putString(input: String): IO[Unit] = outputRef.update(input +: _)
        }
        (console, outputRef)
      }

      console
        .flatMap {
          case (console, outputRef) =>
            WordStream
              .resource[IO, Event](console)(List(testScriptPath))
              .tupleRight(outputRef)
        }
        .use {
          case (wordStream, outputRef) =>
            wordStream.stream.compile.toList >> outputRef.get.map { output =>
              output.size shouldBe 1
              output.head shouldBe """Unable to decode input: {"invalid":"cica","data":"kecske","timestamp":1}"""
            }
        }
        .unsafeRunSync()
    }

  }

}
