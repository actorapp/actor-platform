package im.actor.api.rpc

import cats.data.Xor
import com.google.protobuf.CodedInputStream
import im.actor.api.rpc.Refs.ApiMessageAttributes
import im.actor.api.rpc.messaging.{ ApiMessage, ApiMessageContainer, ApiMessageReaction, ApiMessageState, ApiQuotedMessage }
import im.actor.server.dialog.DialogCommands.QuotedMessage
import im.actor.server.model.{ HistoryMessage, MessageReaction }
import org.joda.time.DateTime

trait HistoryImplicits {

  implicit class ExtHistoryMessageModel(model: HistoryMessage) {
    def asStruct(
      lastReceivedAt: DateTime,
      lastReadAt:     DateTime,
      reactions:      Seq[MessageReaction],
      attributes:     Option[ApiMessageAttributes] = None
    ): Xor[String, ApiMessageContainer] = {
      val in = CodedInputStream.newInstance(model.messageContentData)
      try {
        Xor.fromEither(ApiMessage.parseFrom(in)) map { messageContent ⇒
          val state = if (model.userId == model.senderUserId) {
            if (model.date.getMillis <= lastReadAt.getMillis)
              Some(ApiMessageState.Read)
            else if (model.date.getMillis <= lastReceivedAt.getMillis)
              Some(ApiMessageState.Received)
            else
              Some(ApiMessageState.Sent)
          } else None // for incoming

          val quotedMessage = None;
          val historyMessag: Option[HistoryMessage] = None;

          val apiQuotedMessage = model.quotedMessagePeer.map(_ ⇒ ApiQuotedMessage(model.quotedMessageRid, None, model.quotedMessagePeer.get.id, historyMessag.get.date.getMillis, quotedMessage)) //TODO hp
          ApiMessageContainer(
            senderUserId = model.senderUserId,
            randomId = model.randomId,
            date = model.date.getMillis,
            message = messageContent,
            state = state,
            reactions = reactions.toVector map (r ⇒ ApiMessageReaction(r.userIds.toVector, r.code)),
            attributes = attributes,
            quotedMessage = apiQuotedMessage
          )
        }
      } catch {
        case e: Exception ⇒ Xor.Left(e.getMessage)
      }
    }
  }

  implicit class ExtQuotedMessageModel(quotedMessage: QuotedMessage) {
    lazy val asStruct: ApiQuotedMessage =
      ApiQuotedMessage(quotedMessage.messageId map (_.value), quotedMessage.publicGroupId map (_.value),
        quotedMessage.senderUserId, quotedMessage.date, Some(quotedMessage.message))
  }

}
