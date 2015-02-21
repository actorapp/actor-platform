package im.actor.server.persist

import im.actor.server.db.Db
import im.actor.server.models
import slick.driver.PostgresDriver.simple._
import Database.dynamicSession

class AuthSmsCodeTable(tag: Tag) extends Table[models.AuthSmsCode](tag, "auth_sms_code") {
  def phoneNumber = column[Long]("phone_number", O.PrimaryKey)
  def smsHash = column[String]("sms_hash")
  def smsCode = column[String]("sms_code")

  def * = (phoneNumber, smsHash, smsCode) <> (models.AuthSmsCode.tupled, models.AuthSmsCode.unapply)
}

object AuthSmsCode {
  val table = TableQuery[AuthSmsCodeTable]
}
