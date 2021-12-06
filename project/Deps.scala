import sbt._

object Deps {
  object Cats {
    val version: String = "2.3.1"
    val effectVersion: String = "2.3.1"

    def core: ModuleID = "org.typelevel" %% "cats-core" % version
    def kernel: ModuleID = "org.typelevel" %% "cats-kernel" % version

    def common: List[ModuleID] = List(core, kernel, effect)

    def effect: ModuleID = "org.typelevel" %% "cats-effect" % effectVersion
    def laws: ModuleID = "org.typelevel" %% "cats-laws" % version

    def `effect-laws`: ModuleID = "org.typelevel" %% "cats-effect-laws" % effectVersion % Test
  }

  def magnolia: ModuleID = "com.propensive" %% "magnolia" % "0.17.0"

  object Circe {
    private val version: String = "0.13.0"

    def core: ModuleID = "io.circe" %% "circe-core" % version
    def generic: ModuleID = "io.circe" %% "circe-generic" % version
    def `generic-extras`: ModuleID = "io.circe" %% "circe-generic-extras" % version
    def numbers: ModuleID = "io.circe" %% "circe-numbers" % version
    def parser: ModuleID = "io.circe" %% "circe-parser" % version
    def fs2 = "io.circe" % "circe-fs2_2.13" % "0.13.0"

    def common: List[ModuleID] = List(core, generic, `generic-extras`)

    def all: List[ModuleID] = List(core, generic, `generic-extras`, numbers, parser, fs2)
  }

  object Pureconfig {
    val version: String = "0.14.0"

    def core: ModuleID = "com.github.pureconfig" %% "pureconfig-core" % version
    def generic: ModuleID = "com.github.pureconfig" %% "pureconfig-generic" % version
    def `cats-effect`: ModuleID = "com.github.pureconfig" %% "pureconfig-cats-effect" % version

    def all: List[ModuleID] = List(core, generic, `cats-effect`)
  }

  object Enumeratum {
    val version: String = "1.6.1"

    def enumeratum: ModuleID = "com.beachape" %% "enumeratum" % version

    def common: List[ModuleID] = List(enumeratum)

    def circe: ModuleID = "com.beachape" %% "enumeratum-circe" % version
  }

  object Fs2 {
    val version: String = "2.5.0"
    def process = "eu.monniot" %% "fs2-process" % "0.3.0"

    def core: ModuleID = "co.fs2" %% "fs2-core" % version
    def io = "co.fs2" %% "fs2-io" % version
    def text = "co.fs2" %% "fs2-text" % version

    def all = List(core, io, process)
  }

  lazy val os = "com.lihaoyi" %% "os-lib" % "0.7.1"

  object Tapir {
    private val version: String = "0.16.16"

    def core: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-core" % version
    def http4s: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % version
    def `json-circe`: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % version
    def cats: ModuleID = "com.softwaremill.sttp.tapir" %% "tapir-cats" % version

    def all = List(core, http4s, `json-circe`, cats)
  }

  object Http4s {
    private val version: String = "0.21.8"

    def core: ModuleID = "org.http4s" %% "http4s-core" % version
    def `blaze-server`: ModuleID = "org.http4s" %% "http4s-blaze-server" % version
    def server: ModuleID = "org.http4s" %% "http4s-server" % version
    def common: List[ModuleID] = List(core, `blaze-server`, server)

    def circe: ModuleID = "org.http4s" %% "http4s-circe" % version
  }

  object Scalatest {
    val version: String = "3.2.2"
    def root: ModuleID = "org.scalatest" %% "scalatest" % version
    def core: ModuleID = "org.scalatest" %% "scalatest-core" % version
    def mustmatchers: ModuleID = "org.scalatest" %% "scalatest-mustmatchers" % version
    def wordspec: ModuleID = "org.scalatest" %% "scalatest-wordspec" % version
    def `scalatest+scalacheck`: ModuleID = "org.scalatestplus" %% "scalacheck-1-15" % (version + ".0")
    val all = List(root, core, mustmatchers, wordspec, `scalatest+scalacheck`)
  }

}
