import Deps._

name := "coralogix"

version := "0.1"

scalaVersion := "2.13.7"
libraryDependencies ++=
  Cats.common ++
    Pureconfig.all ++
    Circe.all ++
    Fs2.all ++
    Tapir.all ++
    Http4s.common ++
    List(Deps.os) ++
    Scalatest.all.map(_ % Test) ++
    List(Http4s.circe).map(_ % Test)
