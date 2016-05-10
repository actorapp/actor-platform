package im.actor.server.notify

import akka.actor.ActorSystem.Settings
import akka.dispatch.{ PriorityGenerator, UnboundedPriorityMailbox }
import com.typesafe.config.Config
import im.actor.server.notify.NotifyProcessorCommands.CancelNotify
import im.actor.server.presences.{ PresenceState, Presences }

class NotifyPriorityMailbox(settings: Settings, config: Config)
  extends UnboundedPriorityMailbox(
    PriorityGenerator {
      case PresenceState(_, Presences.Online, _) ⇒ 0
      case _: CancelNotify                       ⇒ 0
      case _                                     ⇒ 1
    }
  )