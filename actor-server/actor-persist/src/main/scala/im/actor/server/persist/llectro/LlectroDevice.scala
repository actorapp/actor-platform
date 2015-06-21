package im.actor.server.persist.llectro

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class LlectroDeviceTable(tag: Tag) extends Table[models.llectro.LlectroDevice](tag, "llectro_devices") {
  def authId = column[Long]("auth_id", O.PrimaryKey)
  def screenWidth = column[Int]("screen_width")
  def screenHeight = column[Int]("screen_height")

  def * = (authId, screenWidth, screenHeight) <> (models.llectro.LlectroDevice.tupled, models.llectro.LlectroDevice.unapply)
}

object LlectroDevice {
  val devices = TableQuery[LlectroDeviceTable]

  def create(device: models.llectro.LlectroDevice) =
    devices += device

  def create(authId: Long, screenWidth: Int, screenHeight: Int) =
    devices += models.llectro.LlectroDevice(authId, screenWidth, screenHeight)

  def find(authId: Long) =
    devices.filter(_.authId === authId).result.headOption

  def find(authIds: Set[Long]) =
    devices.filter(_.authId inSet authIds).result
}