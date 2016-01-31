package im.actor.server.persist

import im.actor.server.model.AvatarData
import slick.driver.PostgresDriver.api._

object AvatarOfColumnType {
  implicit val avatarOfTypeMapper =
    MappedColumnType.base[AvatarData.TypeVal, Int](_.toInt, AvatarData.TypeVal.fromInt)
}
