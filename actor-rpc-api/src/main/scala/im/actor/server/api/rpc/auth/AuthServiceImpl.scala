package im.actor.server.api.rpc.auth

import im.actor.server.api.helpers.PhoneNumber
import im.actor.api.rpc._
import im.actor.api.rpc.auth._
import im.actor.api.rpc.misc._
import im.actor.server.models
import im.actor.server.persist

import scala.concurrent._, forkjoin.ThreadLocalRandom

import scalaz._, std.either._
import shapeless._
import slick.dbio.DBIO
import slick.driver.PostgresDriver.api._

trait AuthServiceImpl extends AuthService {
  val db: Database

  override def handleGetAuthSessions(authId: Long, optUserId: Option[Int]): Future[HandlerResult[ResponseGetAuthSessions]] =
    throw new NotImplementedError()

  override def handleSendAuthCode(
    authId: Long, optUserId: Option[Int], userPhoneNumber: Long, appId: Int, apiKey: String
  ): Future[HandlerResult[ResponseSendAuthCode]] = {
    PhoneNumber.normalizeLong(userPhoneNumber) match {
      case None =>
        Future.successful(-\/(Errors.PhoneNumberInvalid))
      case Some(normalizedPhoneNumber) =>
        val action = persist.AuthSmsCode.findByPhoneNumber(normalizedPhoneNumber).headOption.flatMap {
          case Some(models.AuthSmsCode(_, smsHash, smsCode)) =>
            DBIO.successful(normalizedPhoneNumber :: smsHash :: smsCode :: HNil)
          case None =>
            val smsHash = genSmsHash()
            val smsCode = normalizedPhoneNumber.toString match {
              case strNumber if strNumber.startsWith("7555") => strNumber(4).toString * 4
              case _ => genSmsCode()
            }
            for (
              _ <- persist.AuthSmsCode.create(
                phoneNumber = normalizedPhoneNumber, smsHash = smsHash, smsCode = smsCode
              )
            ) yield (normalizedPhoneNumber :: smsHash :: smsCode :: HNil)
        }.flatMap { res =>
          persist.UserPhone.exists(normalizedPhoneNumber) map (res :+ _)
        }.map {
          case number :: smsHash :: smsCode :: isRegistered :: HNil =>
            sendSmsCode(authId, number, smsCode)
            Ok(ResponseSendAuthCode(smsHash, isRegistered), Vector.empty)
        }
        db.run(action.transactionally)
    }
  }


  override def handleSignIn(
    authId: Long, optUserId: Option[Int],
    phoneNumber: Long,
    smsHash:     String,
    smsCode:     String,
    publicKey:   Array[Byte],
    deviceHash:  Array[Byte],
    deviceTitle: String,
    appId:       Int,
    appKey:      String
  ): Future[HandlerResult[ResponseAuth]] =
    throw new NotImplementedError()

  override def handleSendAuthCall(
    authId: Long, optUserId: Option[Int], phoneNumber: Long, smsHash: String, appId: Int, apiKey: String
  ): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleSignOut(authId: Long, optUserId: Option[Int]): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleSignUp(
    authId: Long,
    optUserId: Option[Int],
    phoneNumber: Long,
    smsHash: String,
    smsCode: String,
    name: String,
    publicKey: Array[Byte],
    deviceHash: Array[Byte],
    deviceTitle: String,
    appId: Int,
    appKey: String,
    isSilent: Boolean
  ): Future[HandlerResult[ResponseAuth]] =
    throw new NotImplementedError()

  override def handleTerminateAllSessions(authId: Long, optUserId: Option[Int]): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  override def handleTerminateSession(authId: Long, optUserId: Option[Int], id: Int): Future[HandlerResult[ResponseVoid]] =
    throw new NotImplementedError()

  private def sendSmsCode(authId: Long, phoneNumber: Long, code: String): Unit = {

  }

  private def genSmsCode() = ThreadLocalRandom.current.nextLong().toString.dropWhile(c => c == '0' || c == '-').take(6)

  private def genSmsHash() = ThreadLocalRandom.current.nextLong().toString
}
