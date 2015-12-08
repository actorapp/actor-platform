package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.model

object UserStateColumnType {
  implicit val userStateColumnType =
    MappedColumnType.base[model.UserState, Int](_.toInt, model.UserState.fromInt)
}
