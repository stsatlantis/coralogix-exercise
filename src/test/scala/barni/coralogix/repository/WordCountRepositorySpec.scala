package barni.coralogix.repository

import barni.coralogix.UnitSpec
import cats.effect.concurrent.Ref
import cats.effect.{Resource, SyncIO}
import cats.syntax.flatMap._
import org.scalacheck.{Arbitrary, Gen}

final class WordCountRepositorySpec extends UnitSpec {

  "WordCountRepository" when {
    implicit val tupleCountable: Countable[(String, String)] = new Countable[(String, String)] {
      override def partitionKey(input: (String, String)): String = input._1
      override def sortKey(input: (String, String)): String = input._2
    }

    "add elem" should {
      "store new element" in runSyncTest {
        val testData = "kecske" -> "kacsa"
        Resource
          .liftF(Ref[SyncIO].of(WordCountRepository.Storage.empty))
          .map(storage => (storage, WordCountRepository(storage)))
          .use {
            case (ref, repo) =>
              repo.add(testData) >> ref.get.map(
                _ shouldBe Map(testData -> 1L)
              )
          }
      }

      "update counter for matching data" in runSyncTest {
        val testData = "kecske" -> "kacsa"
        Resource
          .liftF(Ref[SyncIO].of(Map(testData -> 1L)))
          .map(storage => (storage, WordCountRepository(storage)))
          .use {
            case (ref, repo) =>
              repo.add(testData) >> ref.get.map(
                _ shouldBe Map(testData -> 2L)
              )
          }
      }

      "not update counter for mismatching data" in runSyncTest {
        val testData = "kecske" -> "kacsa"
        val testData2 = "kecske" -> "cica"
        Resource
          .liftF(Ref[SyncIO].of(Map(testData -> 1L)))
          .map(storage => (storage, WordCountRepository(storage)))
          .use {
            case (ref, repo) =>
              repo.add(testData2) >> ref.get.map(
                _ shouldBe Map(testData -> 1L, testData2 -> 1L)
              )
          }
      }
    }

    "wipe storage" should {

      implicit val genMapSSL: Gen[Map[(String, String), Long]] = Arbitrary.arbitrary[List[((String, String), Long)]].map(_.toMap)

      "empty storage when it has data" in {
        forAll(genMapSSL) { storage =>
          runSyncTest {
            Resource
              .liftF(Ref[SyncIO].of(storage))
              .map(storage => (storage, WordCountRepository(storage)))
              .use {
                case (ref, repo) =>
                  repo.wipe() >> ref.get.map(_ shouldBe Map.empty[(String, String), Long])
              }
          }
        }
      }

    }

    "retrieve all element" should {
      "return empty list" when {
        "storage is empty" in runSyncTest {
          Resource
            .liftF(Ref[SyncIO].of(Map.empty[(String, String), Long]))
            .map(WordCountRepository(_))
            .use { repo => repo.all().map(_ shouldBe List.empty[WordCountRepository.WordCount]) }
        }
        "storage was wiped" in runSyncTest {
          val testData = "kecske" -> "kacsa"
          Resource
            .liftF(Ref[SyncIO].of(Map(testData -> 1L)))
            .map(WordCountRepository(_))
            .use { repo =>
              repo.wipe() >>
                repo.all().map(_ shouldBe List.empty[WordCountRepository.WordCount])

            }
        }
      }
    }
  }

}
