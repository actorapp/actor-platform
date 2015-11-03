package im.actor.server.persist

import slick.driver.PostgresDriver.api._

import im.actor.server.model

final class AuthSmsCodeObsoleteTable(tag: Tag) extends Table[model.AuthSmsCodeObsolete](tag, "auth_sms_codes_obsolete") {
  def id = column[Long]("id", O.PrimaryKey)
  def phoneNumber = column[Long]("phone_number")
  def smsHash = column[String]("sms_hash")
  def smsCode = column[String]("sms_code")
  def isDeleted = column[Boolean]("is_deleted")

  def * = (id, phoneNumber, smsHash, smsCode, isDeleted) <> (model.AuthSmsCodeObsolete.tupled, model.AuthSmsCodeObsolete.unapply)
}

object AuthSmsCodeObsoleteRepo {
  val codes = TableQuery[AuthSmsCodeObsoleteTable]

  def byPhoneNumber(number: Rep[Long]) =
    codes.filter(c ⇒ c.phoneNumber === number && c.isDeleted === false)
  private val byPhoneNumberC = Compiled(byPhoneNumber _)

  def create(id: Long, phoneNumber: Long, smsHash: String, smsCode: String) =
    codes += model.AuthSmsCodeObsolete(id, phoneNumber, smsHash, smsCode)

  def findByPhoneNumber(number: Long) =
    byPhoneNumberC(number).result

  def deleteByPhoneNumber(number: Long) =
    byPhoneNumber(number).map(_.isDeleted).update(true)
}
