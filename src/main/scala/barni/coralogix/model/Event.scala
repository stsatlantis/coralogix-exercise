package barni.coralogix.model

import barni.coralogix.repository.Countable
import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder

final case class Event(eventType: String, data: String, timestamp: Long)

object Event {

  implicit val customConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val inputRecordDecoder: Decoder[Event] = {
    deriveConfiguredDecoder
  }

  implicit val countable: Countable[Event] = new Countable[Event] {
    override def partitionKey(input: Event): String = input.eventType
    override def sortKey(input: Event): String = input.data
  }
}
