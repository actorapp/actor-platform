package im.actor.server.persist

import im.actor.server.model.UserState
import slick.driver.PostgresDriver.api._

object UserStateColumnType {
  implicit val userStateColumnType =
    MappedColumnType.base[UserState, Int](_.toInt, UserState.fromInt)
}
