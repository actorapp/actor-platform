package im.actor.server.activation

import akka.actor.ActorSystem
import cats.data.Xor
import im.actor.server.activation.Activation.{EmailCode, SmsCode, Code}
import im.actor.server.activation.gate.GateConfig
import im.actor.server.db.DbExtension
import im.actor.server.persist.{UserPhoneRepo, UserRepo}
import im.actor.server.persist.auth.GateAuthCodeRepo
import im.actor.server.db.ActorPostgresDriver.api._

import scala.concurrent.{Await, Future}
import scala.util.Try

trait CodeHandler {
  //or not private
  private val hashes: scala.collection.mutable.Set[String] = scala.collection.mutable.Set.empty[String]

  protected final def sendCodeInternal(hash: String) = new PartialFunction[Code, Future[CodeFailure Xor Unit]] {
    override def isDefinedAt(c: Code): Boolean = sendCode(hash).isDefinedAt(c)
    override def apply(c: Code): Future[Xor[CodeFailure, Unit]] = {
      hashes.add(hash)
      sendCode(hash)(c)
    }
  }

  protected final def validateInternal(code: String) = new PartialFunction[String, Future[ValidationResponse]] {
    override def isDefinedAt(hash: String): Boolean = hashes contains hash
    override def apply(hash: String): Future[ValidationResponse] = validate(code, hash)
  }

  protected final def cleanupInternal = new PartialFunction[String, Future[Unit]] {
    override def isDefinedAt(hash: String): Boolean = hashes contains hash
    override def apply(hash: String): Future[Unit] = {
      hashes remove hash
      cleanup(hash)
    }
  }

  def sendCode(hash: String): PartialFunction[Code, Future[CodeFailure Xor Unit]]

  def validate(code: String, hash: String): Future[ValidationResponse]

  def cleanup(hash: String): Future[Unit]
}

final class GateCodeHandler(config: GateConfig)(implicit system: ActorSystem) extends CodeHandler {

  private val db = DbExtension(system).db

  override def sendCode(hash: String) = {
    case SmsCode(phone, code) =>
      //....
      //gate activation logic
      //send code via http api, etc
      Future.successful(Xor.right(()))
  }

  override def cleanup(hash: String): Future[Unit] = db.run(GateAuthCodeRepo.delete(hash).map(_ ⇒ ()))

  override def validate(code: String, hash: String): Future[ValidationResponse] =
    //....
    //gate activation logic
    //validate code via http api, etc
    Future.successful(Validated)
}

final class GateCodeHandler(config: GateConfig)(implicit system: ActorSystem) extends CodeHandler {

  private val db = DbExtension(system).db

  override def sendCode(hash: String) = {
    case SmsCode(phone, code) =>
      //....
      //gate activation logic
      //send code via http api, etc
      Future.successful(Xor.right(()))
  }

  override def cleanup(hash: String): Future[Unit] = db.run(GateAuthCodeRepo.delete(hash).map(_ ⇒ ()))

  override def validate(code: String, hash: String): Future[ValidationResponse] =
    //....
    //gate activation logic
    //validate code via http api, etc
    Future.successful(Validated)
}

object BotConfig {
  def load: Try[BotConfig] = ???
}

case class BotConfig(nickname: String)

//This one does not look good because of blocking methods
final class BotCodeHandler(config: BotConfig)(implicit system: ActorSystem) extends CodeHandler {

  import scala.concurrent.duration._

  private val db = DbExtension(system).db

  override def sendCode(hash: String) = {
    case SmsCode(phone, code) if isOnline(phone) =>
      //....
      //bot activation logic
      //send code via bot api
      Future.successful(Xor.right(()))
    case EmailCode(email, code) if isOnline(email) =>
  }

  override def cleanup(hash: String): Future[Unit] = //cleanup logic

  override def validate(code: String, hash: String): Future[ValidationResponse] =
    //....
    //bot activation logic
    //validate code via bot api, etc
    Future.successful(Validated)

  //FIXME: this will be blocking method. How to fix it?
  def isOnline(phone: Long): Boolean =
    Await.result(db.run(for {
      optPhone <- UserPhoneRepo.findByPhoneNumber(phone).headOption
      exists <- optPhone map { phone => UserRepo.find(phone.userId) map (_.isDefined) } getOrElse DBIO.successful(false)
    //check that user online
    } yield exists), 5.seconds)

  //same as previous
  def isOnline(email: String) = ???

}


object Main {

  //well, if we have defined config indicates that this activation handler is enabled
  val botConfig = BotConfig.load.toOption
  val gateConfig = GateConfig.load.toOption

  // list of code handlers.
  // Bot handler have highest priority - we need to handle cases when users are still online.
  // Gate handler comes after this one,
  // and I'd put Telesign handler after this one, cause it handles CallActivation
  val handlers = List(
    botConfig map { c => new BotCodeHandler(c) },
    gateConfig map { c => new GateCodeHandler(c) }
  ) flatten

  val finalActivationMonster = new ActivationContext(handlers)

}

//not sure that ActivationContext should be CodeHandler itself
class ActivationContext(handlers: List[CodeHandler])(implicit system: ActorSystem) extends CodeHandler {

  private val reverseHandlers = handlers.reverse

  //not sure about this
  override def sendCode(hash: String): PartialFunction[Code, Future[Xor[CodeFailure, Unit]]] = {
    (reverseHandlers foldRight PartialFunction.empty[Code, Future[Xor[CodeFailure, Unit]]]) { case (el, acc) =>
      acc orElse el.sendCodeInternal(hash)
    }
  }

  //not sure about this
  override def cleanup(hash: String): Future[Unit] = {
    val function = (reverseHandlers foldRight PartialFunction.empty[String, Future[Unit]]) { case (el, acc) =>
      acc orElse el.cleanupInternal
    }
    function(hash)
  }

  //not sure about this
  override def validate(code: String, hash: String): Future[ValidationResponse] = {
    val function = (reverseHandlers foldRight PartialFunction.empty[String, Future[ValidationResponse]]) { case (el, acc) =>
      acc orElse el.validateInternal(code)
    }
    function(hash)
  }
}
