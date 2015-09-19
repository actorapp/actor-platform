package im.actor.server.dialog

import im.actor.serialization.ActorSerializer
import im.actor.server.office.{Processor, ProcessorState}

object DialogProcessor {
  def register(): Unit = {
    ActorSerializer.register(
      40000 -> classOf[DialogCommands.SendMessage],
      40001 -> classOf[DialogCommands.MessageReceived],
      40002 -> classOf[DialogCommands.MessageReceivedAck],
      40003 -> classOf[DialogCommands.MessageRead],
      40004 -> classOf[DialogCommands.MessageReadAck]
    )
  }
}

trait DialogProcessor[State <: ProcessorState, Event <: AnyRef] extends Processor[State, Event]