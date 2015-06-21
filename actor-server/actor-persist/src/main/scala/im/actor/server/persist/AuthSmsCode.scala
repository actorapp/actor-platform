package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class AuthSmsCodeTable(tag: Tag) extends Table[models.AuthSmsCode](tag, "auth_sms_codes") {
  def phoneNumber = column[Long]("phone_number", O.PrimaryKey)

  def smsHash = column[String]("sms_hash")

  def smsCode = column[String]("sms_code")

  def * = (phoneNumber, smsHash, smsCode) <> (models.AuthSmsCode.tupled, models.AuthSmsCode.unapply)
}

object AuthSmsCode {
  val codes = TableQuery[AuthSmsCodeTable]

  def create(phoneNumber: Long, smsHash: String, smsCode: String) =
    codes += models.AuthSmsCode(phoneNumber, smsHash, smsCode)

  def findByPhoneNumber(number: Long) = codes.filter(_.phoneNumber === number).result

  def deleteByPhoneNumber(number: Long) = codes.filter(_.phoneNumber === number).delete
}
