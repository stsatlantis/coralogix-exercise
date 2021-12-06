package barni.coralogix.service

import barni.coralogix.UnitSpec
import barni.coralogix.repository.{Countable, WordCountRepository, WordStream}
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.option._
import org.scalatest.AppendedClues

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class WordCountServiceSpec extends UnitSpec with AppendedClues {

  val resetTimeoutInMillis: Int = 700
  val samplingIntervalInMillis = 40
  val samplingCount = 25

  implicit val tupleCountable: Countable[(String, String)] = new Countable[(String, String)] {
    override def partitionKey(input: (String, String)): String = input._1
    override def sortKey(input: (String, String)): String = input._2
  }

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit val concurrent: ConcurrentEffect[IO] = IO.ioConcurrentEffect
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)

  def slowSource(input: (String, String)*)(delayed: FiniteDuration): WordStream[IO, (String, String)] =
    new WordStream[IO, (String, String)] {
      override def stream: fs2.Stream[IO, (String, String)] = fs2.Stream[IO, (String, String)](input: _*).metered(delayed)
    }

  val config: WordCountService.Config = WordCountService.Config(10, 100.millis, resetTimeoutInMillis.millis)

  def createService(input: (String, String)*): IO[(WordCountService[IO], WordCountRepository[IO, (String, String)])] =
    for {
      storage <- Ref[IO].of(WordCountRepository.Storage.empty)
      repo = WordCountRepository[IO, (String, String)](storage)
      service = WordCountService(config, slowSource(input: _*)(40.millis), repo)
    } yield (service, repo)

  def uncreasedUntil(input: List[(Int, Long)]): Option[(Int, Long)] = {
    @tailrec
    def go(input: List[(Int, Long)], acc: Option[(Int, Long)]): Option[(Int, Long)] = {
      input match {
        case Nil => acc
        case (head @ (_, value)) :: tail =>
          acc match {
            case None => go(tail, head.some)
            case Some((_, prevValue)) =>
              if (prevValue > value) acc
              else
                go(tail, head.some)
          }
      }
    }
    go(input, none)
  }

  "WordCountService" should {

    s"reset counter after $resetTimeoutInMillis ms" in runIoTest {
      Ref[IO]
        .of(List.empty[(Int, Long)])
        .flatMap { ref =>
          createService(List.fill(20)("key" -> "value"): _*)
            .flatMap {
              case (service, repo) =>
                service
                  .process()
                  .compile
                  .drain
                  .start
                  .flatMap { fiber =>
                    List
                      .fill(samplingCount)(samplingIntervalInMillis.millis)
                      .zipWithIndex
                      .traverse_ {
                        case (timeout, idx) =>
                          Timer[IO].sleep(timeout) >>
                            repo
                              .all()
                              .flatTap(e => ref.update((idx -> e.headOption.fold(0L)(_.count)) +: _))
                      } >> fiber.cancel >> ref.get

                  }
            }
        }
        .map { collectedData =>
          val dataInOrder = collectedData.reverse
          uncreasedUntil(dataInOrder) match {
            case None => fail("No Data was collected")
            case Some((sampleCount, value)) =>
              value shouldBe 14
              sampleCount shouldBe resetTimeoutInMillis / samplingIntervalInMillis
          }
        }
    }
  }

}
