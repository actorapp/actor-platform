package im.actor.server.sequence.operations

import akka.http.scaladsl.util.FastFuture
import im.actor.server.model.AuthSession
import im.actor.server.model.push._
import im.actor.server.persist.AuthSessionRepo
import im.actor.server.persist.push.{ ActorPushCredentialsRepo, ApplePushCredentialsRepo, GooglePushCredentialsRepo }
import im.actor.server.sequence.SeqUpdatesExtension
import im.actor.server.sequence.UserSequenceCommands.{ Envelope, RegisterPushCredentials, UnregisterPushCredentials, UnregisterPushCredentialsAck }
import scodec.bits.BitVector
import akka.pattern.ask

import scala.concurrent.Future

trait PushOperations { this: SeqUpdatesExtension ⇒

  def registerGCMPushCredentials(creds: GCMPushCredentials) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withGcm(creds))

  def registerFirebasePushCredentials(creds: FirebasePushCredentials) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withFirebase(creds))

  def registerApplePushCredentials(creds: ApplePushCredentials) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withApple(creds))

  def registerActorPushCredentials(creds: ActorPushCredentials) =
    registerPushCredentials(creds.authId, RegisterPushCredentials().withActor(creds))

  // TODO: real future
  private def registerPushCredentials(authId: Long, register: RegisterPushCredentials) =
    withAuthSession(authId) { session ⇒
      region.ref ! Envelope(session.userId).withRegisterPushCredentials(register)
      FastFuture.successful(())
    }

  def unregisterAllPushCredentials(authId: Long): Future[Unit] =
    findAllPushCredentials(authId) map { creds ⇒
      creds map (c ⇒ unregisterPushCredentials(c.authId, makeUnregister(c)))
    }

  def unregisterActorPushCredentials(endpoint: String): Future[Unit] =
    db.run(ActorPushCredentialsRepo.findByTopic(endpoint).headOption) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withActor(creds))
      case None ⇒
        log.warning("Actor push credentials not found for endpoint: {}", endpoint)
        FastFuture.successful(())
    }

  def unregisterApplePushCredentials(token: Array[Byte]): Future[Unit] =
    db.run(ApplePushCredentialsRepo.findByToken(token).headOption) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withApple(creds))
      case None ⇒
        log.warning("Apple push credentials not found for token: {}", BitVector(token).toHex)
        FastFuture.successful(())
    }

  def unregisterGCMPushCredentials(token: String): Future[Unit] =
    db.run(GooglePushCredentialsRepo.findByToken(token)) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withGcm(creds))
      case None ⇒
        log.warning("Google push credentials not found for token: {}", token)
        FastFuture.successful(())
    }

  def unregisterFirebasePushCredentials(token: String): Future[Unit] =
    firebaseKv.findByToken(token) flatMap {
      case Some(creds) ⇒
        unregisterPushCredentials(creds.authId, UnregisterPushCredentials().withFirebase(creds))
      case None ⇒
        log.warning("Google push credentials not found for token: {}", token)
        FastFuture.successful(())
    }

  private def unregisterPushCredentials(authId: Long, unregister: UnregisterPushCredentials): Future[Unit] =
    withAuthSession(authId) { session ⇒
      (region.ref ? Envelope(session.userId).withUnregisterPushCredentials(unregister)).mapTo[UnregisterPushCredentialsAck] map (_ ⇒ ())
    }

  private def makeUnregister: PartialFunction[PushCredentials, UnregisterPushCredentials] = {
    case actor: ActorPushCredentials       ⇒ UnregisterPushCredentials().withActor(actor)
    case apple: ApplePushCredentials       ⇒ UnregisterPushCredentials().withApple(apple)
    case gcm: GCMPushCredentials           ⇒ UnregisterPushCredentials().withGcm(gcm)
    case firebase: FirebasePushCredentials ⇒ UnregisterPushCredentials().withFirebase(firebase)
  }

  private def findAllPushCredentials(authId: Long): Future[Seq[PushCredentials]] =
    db.run(for {
      google ← GooglePushCredentialsRepo.find(authId)
      apple ← ApplePushCredentialsRepo.find(authId)
      actor ← ActorPushCredentialsRepo.find(authId)
    } yield Seq(google, apple, actor).flatten)

  private def withAuthSession[A](authId: Long)(f: AuthSession ⇒ Future[A]): Future[A] = {
    db.run(AuthSessionRepo.findByAuthId(authId)) flatMap {
      case Some(session) ⇒ f(session)
      case None ⇒
        val err = new RuntimeException("AuthSession not found")
        log.error(err, err.getMessage)
        throw err
    }
  }

}
