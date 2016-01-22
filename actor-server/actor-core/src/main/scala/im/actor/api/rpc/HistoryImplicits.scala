package im.actor.api.rpc

import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.messaging.{ ApiMessage, ApiMessageContainer, ApiMessageReaction, ApiMessageState }
import im.actor.server.model.{ HistoryMessage, MessageReaction }
import org.joda.time.DateTime

trait HistoryImplicits {

  implicit class ExtHistoryMessageModel(model: HistoryMessage) {
    def asStruct(lastReceivedAt: DateTime, lastReadAt: DateTime, reactions: Seq[MessageReaction]): ApiMessageContainer = {
      val in = CodedInputStream.newInstance(model.messageContentData)
      try {
        ApiMessage.parseFrom(in) match {
          case Right(messageContent) ⇒
            val state = if (model.userId == model.senderUserId) {
              if (model.date.getMillis <= lastReadAt.getMillis) {
                Some(ApiMessageState.Read)
              } else if (model.date.getMillis <= lastReceivedAt.getMillis) {
                Some(ApiMessageState.Received)
              } else {
                Some(ApiMessageState.Sent)
              }
            } else {
              None // for incoming
            }

            ApiMessageContainer(
              senderUserId = model.senderUserId,
              randomId = model.randomId,
              date = model.date.getMillis,
              message = messageContent,
              state = state,
              reactions = reactions.toVector map (r ⇒ ApiMessageReaction(r.userIds.toVector, r.code))
            )
          case Left(e) ⇒ throw new Exception(s"Failed to parse message content: $e")
        }
      } catch {
        case e: Throwable ⇒
          throw e
      }
    }
  }

}
