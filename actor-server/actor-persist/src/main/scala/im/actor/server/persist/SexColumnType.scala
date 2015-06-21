package im.actor.server.persist

import im.actor.server.models
import slick.driver.PostgresDriver.api._

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[models.Sex, Int](_.toInt, models.Sex.fromInt)
}
