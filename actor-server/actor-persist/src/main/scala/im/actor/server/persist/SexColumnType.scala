package im.actor.server.persist

import im.actor.server.model.Sex
import slick.driver.PostgresDriver.api._

object SexColumnType {
  implicit val sexColumnType =
    MappedColumnType.base[Sex, Int](_.toInt, Sex.fromInt)
}
