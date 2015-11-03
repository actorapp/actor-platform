package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.model

object MessageStateColumnType {
  implicit val messageStateColumnType =
    MappedColumnType.base[model.MessageState, Int](_.toInt, model.MessageState.fromInt)
}
