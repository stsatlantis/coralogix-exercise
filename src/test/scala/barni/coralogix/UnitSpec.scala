package barni.coralogix

import cats.effect.{IO, SyncIO}
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

trait UnitSpec extends AnyWordSpec with Matchers with ScalaCheckPropertyChecks {

  def runSyncTest(body: => SyncIO[Assertion]): Assertion =
    runIoTest(body.toIO)

  def runIoTest(body: => IO[Assertion]): Assertion =
    body.unsafeRunSync()

}
