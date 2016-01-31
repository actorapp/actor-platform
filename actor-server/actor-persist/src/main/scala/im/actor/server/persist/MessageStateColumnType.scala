package im.actor.server.persist

import im.actor.server.model.MessageState
import slick.driver.PostgresDriver.api._

object MessageStateColumnType {
  implicit val messageStateColumnType =
    MappedColumnType.base[MessageState, Int](_.toInt, MessageState.fromInt)
}
