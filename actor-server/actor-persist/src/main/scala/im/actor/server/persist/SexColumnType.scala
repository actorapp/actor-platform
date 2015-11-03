package im.actor.server.persist

import im.actor.server.model
import slick.driver.PostgresDriver.api._

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[model.Sex, Int](_.toInt, model.Sex.fromInt)
}
