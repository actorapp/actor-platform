package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.models

object UserStateColumnType {
  implicit val userStateColumnType =
    MappedColumnType.base[models.UserState, Int](_.toInt, models.UserState.fromInt)
}
