package im.actor.server.api.rpc.service

import im.actor.api.{ rpc => api }
import im.actor.server.api.rpc.RpcApiService
import im.actor.server.push.{ WeakUpdatesManager, SeqUpdatesManager }
import im.actor.server.session.Session
import im.actor.server.{ models, persist }

class AuthServiceSpec extends BaseServiceSpec {
  def is = sequential ^ s2"""

  AuthService
    SendAuthCode $sendAuthCode

    SignUp       $signUp

    SignIn       $signIn
                 """

  def sendAuthCode = s2"""
    SendAuthCode handler should
      respond ok to valid number ${s.sendAuthCode.e1}
      not fail if number already exists ${s.sendAuthCode.e1}
                       """

  def signUp = s2"""
    SignUp handler should
      respond ok to a valid request ${s.signUp().e1}
                     """

  def signIn = s2"""
    SignIn handler should
      respond with PhoneNumberUnoccupied if phone is not registered ${s.signIn().unoccupied}
      respond ok to a valid request ${s.signIn().valid}
                     """

  object s {
    val seqUpdManagerRegion = SeqUpdatesManager.startRegion()
    val weakUpdManagerRegion = WeakUpdatesManager.startRegion()
    val rpcApiService = system.actorOf(RpcApiService.props())
    val sessionRegion = Session.startRegion(Some(Session.props(rpcApiService, seqUpdManagerRegion, weakUpdManagerRegion)))

    implicit val ec = system.dispatcher
    implicit val service = new auth.AuthServiceImpl(sessionRegion)

    object sendAuthCode {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()

      implicit val clientData = api.ClientData(authId, sessionId, None)

      def e1 = {
        service.handleSendAuthCode(phoneNumber, 1, "apiKey") must beOkLike {
          case api.auth.ResponseSendAuthCode(_, false) => ok
        }.await
      }
    }

    case class signUp() {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()
      val smsHash = getSmsHash(authId, phoneNumber)

      implicit val clientData = api.ClientData(authId, sessionId, None)

      def e1 = {
        service.handleSignUp(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          name = "Wayne Brain",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey",
          isSilent = false
        ) must beOkLike {
          case api.auth.ResponseAuth(_, _, _) => ok
        }.await
      }
    }

    case class signIn() {
      val authId = createAuthId()(service.db)
      val sessionId = createSessionId()
      val phoneNumber = buildPhone()

      implicit val clientData = api.ClientData(authId, sessionId, None)

      def unoccupied = {
        val smsHash = getSmsHash(authId, phoneNumber)

        service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        ) must beErrorLike {
          case service.Errors.PhoneNumberUnoccupied => ok
        }.await
      }

      def valid = {
        createUser(authId, phoneNumber)

        val smsHash = getSmsHash(authId, phoneNumber)

        service.handleSignIn(
          phoneNumber = phoneNumber,
          smsHash = smsHash,
          smsCode = "0000",
          publicKey = Array(1, 2, 3),
          deviceHash = Array(4, 5, 6),
          deviceTitle = "Specs virtual device",
          appId = 1,
          appKey = "appKey"
        ) must beOkLike {
          case rsp: api.auth.ResponseAuth =>
            service.db.run(persist.AuthId.find(authId).head) must be_==(models.AuthId(authId, Some(rsp.user.id))).await and
              (service.db.run(persist.UserPublicKey.find(rsp.user.id, authId).headOption) must beSome[models.UserPublicKey].await)
        }.await
      }
    }

  }

}
