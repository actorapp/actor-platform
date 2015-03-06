package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.models

object AvatarOfColumnType {
  implicit val avatarOfTypeMapper =
    MappedColumnType.base[models.AvatarData.TypeVal, Int](_.toInt, models.AvatarData.TypeVal.fromInt)
}
