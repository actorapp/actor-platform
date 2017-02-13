package im.actor.server.grouppre

import java.time.Instant

import akka.actor.Props
import im.actor.serialization.ActorSerializer
import im.actor.server.cqrs.TaggedEvent

trait GroupEvent extends TaggedEvent {
  val ts: Instant

  override def tags: Set[String] = Set("grouppre")
}

case object StopProcessor

object GroupPreProcessor {
  def register(): Unit =
    ActorSerializer.register( //      30001 → classOf[GroupCommands.Create],
    //
    //      31001 → classOf[GroupQueries.GetIntegrationToken],
    //
    //      32005 → classOf[GroupEvents.Created]
    )

  def persistenceIdFor(groupId: Int): String = s"Grouppre-${groupId}"

  private[grouppre] def props: Props = Props(classOf[GroupPreProcessor])
}

/**
 * Created by 98379720172 on 31/01/17.
 */
private[grouppre] final class GroupPreProcessor {

}
