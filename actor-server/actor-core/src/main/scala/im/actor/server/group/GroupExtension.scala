package im.actor.server.group

import akka.actor._
import akka.util.Timeout
import im.actor.server.api.http.HttpApi
import im.actor.server.db.DbExtension
import im.actor.server.group.http.GroupsHttpHandler
import im.actor.server.migrations.v2.{ MigrationNameList, MigrationTsActions }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

sealed trait GroupExtension extends Extension

final class GroupExtensionImpl(val system: ActorSystem) extends GroupExtension with GroupOperations {
  GroupProcessor.register()

  HttpApi(system).registerRoute("groups") { implicit system ⇒
    new GroupsHttpHandler().routes
  }

  val GroupV2MigrationTs: Long = {
    val optTs = MigrationTsActions.getTimestamp(MigrationNameList.GroupsV2)(DbExtension(system).connector)
    optTs.getOrElse(throw new RuntimeException(s"No Migration timestamp found for ${MigrationNameList.GroupsV2}"))
  }

  lazy val processorRegion: GroupProcessorRegion = GroupProcessorRegion.start()(system)
  lazy val viewRegion: GroupViewRegion = GroupViewRegion(processorRegion.ref)

  implicit val timeout: Timeout = Timeout(20.seconds)
  implicit val ec: ExecutionContext = system.dispatcher
}

object GroupExtension extends ExtensionId[GroupExtensionImpl] with ExtensionIdProvider {
  override def lookup = GroupExtension

  override def createExtension(system: ExtendedActorSystem) = new GroupExtensionImpl(system)
}
