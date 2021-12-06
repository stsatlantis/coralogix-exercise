package barni.coralogix.api

import barni.coralogix.UnitSpec
import barni.coralogix.api.ApiDescription.EventCountResponse
import barni.coralogix.api.ApiDescription.EventCountResponse._
import barni.coralogix.repository.{Countable, WordCountRepository}
import cats.effect.{ContextShift, IO}
import cats.syntax.flatMap._
import io.circe.Decoder
import org.http4s.implicits._
import org.http4s.{circe, EntityDecoder, Method, Request}
import org.scalatest.Assertion

import scala.concurrent.ExecutionContext

final class WordCountApiSpec extends UnitSpec {

  implicit def entityDecoder[R: Decoder]: EntityDecoder[IO, R] = circe.jsonOf[IO, R]

  "WordCountApi" should {

    def runTestFor(expected: EventCountResponse)(setupRepo: WordCountRepository[IO, (String, String)] => IO[Unit]) = {
      runTest { (repo, api) =>
        setupRepo(repo) >>
          api.getCurrentCount.orNotFound
            .run(Request(method = Method.GET, uri = uri"/"))
            .flatMap(e => e.as[EventCountResponse])
            .map {
              _ shouldBe expected
            }

      }
    }

    "return empty count" when {
      val expected = EventCountResponse(List.empty[EventCount])
      "no collection happened" in runTestFor(expected)(_ => IO.unit)

      "repository was reset" in runTestFor(expected)(repo => repo.add("key" -> "value") >> repo.wipe())
    }

    "return proper count" when {
      "some data is collected" in {
        val expected = EventCountResponse(List(EventCount("key", wordCounts = List(WordCount("value", 2L)))))
        runTestFor(expected)(repo => repo.add("key" -> "value") >> repo.add("key" -> "value"))

      }
    }

  }

  private def runTest(test: (WordCountRepository[IO, (String, String)], WordCountApi[IO]) => IO[Assertion]): Assertion = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
    implicit val tupleCountable: Countable[(String, String)] = new Countable[(String, String)] {
      override def partitionKey(input: (String, String)): String = input._1
      override def sortKey(input: (String, String)): String = input._2
    }

    (for {
      repo <- WordCountRepository.resource[IO, (String, String)]()
      api <- WordCountApi.resource(repo)
    } yield (repo, api))
      .use {
        case (repo, api) =>
          test(repo, api)
      }
      .unsafeRunSync()
  }

}
