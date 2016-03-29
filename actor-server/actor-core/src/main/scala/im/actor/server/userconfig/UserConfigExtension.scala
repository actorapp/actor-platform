package im.actor.server.userconfig

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import im.actor.api.rpc.configs.UpdateParameterChanged
import im.actor.server.db.DbExtension
import im.actor.server.model.configs.Parameter
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.server.user.UserExtension

import scala.concurrent.Future

object UserConfigExtension extends ExtensionId[UserConfigExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new UserConfigExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserConfigExtension
}

final class UserConfigExtension(system: ActorSystem) extends Extension {
  import system.dispatcher

  private lazy val db = DbExtension(system).db
  private lazy val userExt = UserExtension(system)
  private lazy val seqUpdExt = SeqUpdatesExtension(system)

  def fetchParameters(userId: Int): Future[Seq[(String, Option[String])]] = {
    for {
      params ← db.run(ParameterRepo.find(userId))
    } yield params.map(p ⇒ p.key → p.value)
  }

  def editParameter(userId: Int, rawKey: String, value: Option[String]): Future[SeqState] = {
    val key = rawKey.trim

    val update = UpdateParameterChanged(key, value)

    for {
      _ ← db.run(ParameterRepo.createOrUpdate(Parameter(userId, key, value)))
      seqstate ← seqUpdExt.deliverSingleUpdate(userId, update)
    } yield {
      seqUpdExt.reloadSettings(userId)
      seqstate
    }
  }
}