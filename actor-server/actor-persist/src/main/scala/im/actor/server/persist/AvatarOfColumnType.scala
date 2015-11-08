package im.actor.server.persist

import slick.driver.PostgresDriver.api._
import im.actor.server.model

object AvatarOfColumnType {
  implicit val avatarOfTypeMapper =
    MappedColumnType.base[model.AvatarData.TypeVal, Int](_.toInt, model.AvatarData.TypeVal.fromInt)
}
