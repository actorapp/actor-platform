package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.models

object MessageStateColumnType {
  implicit val messageStateColumnType =
    MappedColumnType.base[models.MessageState, Int](_.toInt, models.MessageState.fromInt)
}
