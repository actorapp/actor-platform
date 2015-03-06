package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import im.actor.server.persist
import scala.concurrent._, duration._
import slick.driver.PostgresDriver.api._

trait ServiceSpecHelpers {
  def buildPhone(): Long = {
    75550000000L + scala.util.Random.nextInt(999999)
  }

  def createAuthId(db: Database): Long = {
    val authId = scala.util.Random.nextLong

    Await.result(db.run(persist.AuthId.create(authId, None)), 1.second)
    authId
  }

  def getSmsHash(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService): String = {
    val api.auth.ResponseSendAuthCode(smsHash, _) =
      Await.result(service.handleSendAuthCode(authId, None, phoneNumber, 1, "apiKey"), 1.second).toOption.get._1
    smsHash
  }

  def createUser(authId: Long, phoneNumber: Long)(implicit service: api.auth.AuthService) = {
    val smsHash = getSmsHash(authId, phoneNumber)(service)

    Await.result(service.handleSignUp(
      authId = authId,
      optUserId = None,
      phoneNumber = phoneNumber,
      smsHash = smsHash,
      smsCode = "0000",
      name = "Wayne Brain",
      publicKey = scala.util.Random.nextLong.toBinaryString.getBytes(),
      deviceHash = scala.util.Random.nextLong.toBinaryString.getBytes(),
      deviceTitle = "Specs virtual device",
      appId = 42,
      appKey = "appKey",
      isSilent = false
    ), 1.second)
  }
}
