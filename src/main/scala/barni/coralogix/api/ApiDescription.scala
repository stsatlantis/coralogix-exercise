package barni.coralogix.api

import barni.coralogix.api.ApiDescription.EventCountResponse.EventCount
import io.circe.Codec
import io.circe.generic.semiauto._
import sttp.tapir._
import sttp.tapir.json.circe._

object ApiDescription {

  val getCurrentCount: Endpoint[Unit, Unit, EventCountResponse, Nothing] = endpoint.get
    .out(jsonBody[EventCountResponse])

  final case class EventCountResponse(events: List[EventCount])
  object EventCountResponse {

    implicit val wordCountCodec: Codec[WordCount] = deriveCodec
    implicit val eventCountCodec: Codec[EventCount] = deriveCodec
    implicit val codec: Codec[EventCountResponse] = deriveCodec

    final case class EventCount(eventType: String, wordCounts: List[WordCount])
    final case class WordCount(word: String, count: Long)
  }

}
