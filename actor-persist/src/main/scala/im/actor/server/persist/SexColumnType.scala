package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.models

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[models.Sex, Int](_.toInt, models.Sex.fromInt)
}
