package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class AuthSmsCodeObsoleteTable(tag: Tag) extends Table[models.AuthSmsCodeObsolete](tag, "auth_sms_codes_obsolete") {
  def phoneNumber = column[Long]("phone_number", O.PrimaryKey)
  def smsHash = column[String]("sms_hash")
  def smsCode = column[String]("sms_code")

  def * = (phoneNumber, smsHash, smsCode) <> (models.AuthSmsCodeObsolete.tupled, models.AuthSmsCodeObsolete.unapply)
}

object AuthSmsCodeObsolete {
  val codes = TableQuery[AuthSmsCodeObsoleteTable]

  def create(phoneNumber: Long, smsHash: String, smsCode: String) =
    codes += models.AuthSmsCodeObsolete(phoneNumber, smsHash, smsCode)

  def findByPhoneNumber(number: Long) = codes.filter(_.phoneNumber === number).result

  def deleteByPhoneNumber(number: Long) = codes.filter(_.phoneNumber === number).delete
}
