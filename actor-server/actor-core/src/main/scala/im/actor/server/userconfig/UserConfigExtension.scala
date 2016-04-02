package im.actor.server.userconfig

import akka.actor.{ ActorSystem, ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider }
import im.actor.api.rpc.configs.UpdateParameterChanged
import im.actor.hook.{ Hook2, Hook3, HooksStorage2, HooksStorage3 }
import im.actor.server.db.DbExtension
import im.actor.server.model.configs.Parameter
import im.actor.server.persist.configs.ParameterRepo
import im.actor.server.sequence.{ SeqState, SeqUpdatesExtension }
import im.actor.types._

import scala.concurrent.Future

object UserConfigExtension extends ExtensionId[UserConfigExtension] with ExtensionIdProvider {
  override def createExtension(system: ExtendedActorSystem) = new UserConfigExtension(system)

  override def lookup(): ExtensionId[_ <: Extension] = UserConfigExtension
}

final class UserConfigExtension(system: ActorSystem) extends Extension {

  import system.dispatcher

  private lazy val db = DbExtension(system).db
  private lazy val seqUpdExt = SeqUpdatesExtension(system)

  val hooks = new HooksStorage3[EditParameterHook, Any, UserId, String, Option[String]]()

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

trait EditParameterHook extends Hook3[Any, UserId, String, Option[String]]