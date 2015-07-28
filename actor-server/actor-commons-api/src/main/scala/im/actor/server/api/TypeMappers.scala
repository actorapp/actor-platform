package im.actor.server.api

import com.google.protobuf.{ ByteString, CodedInputStream }
import com.trueaccord.scalapb.TypeMapper
import im.actor.api.rpc.users.{ Sex ⇒ S }
import im.actor.api.rpc.users.Sex.Sex
import org.joda.time.DateTime

import im.actor.api.rpc.files.Avatar
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.peers.Peer

object TypeMappers extends MessageMapper

trait MessageMapper {
  def applyMessage(bytes: ByteString): ApiMessage = {
    if (bytes.size() > 0) {
      val res = ApiMessage.parseFrom(CodedInputStream.newInstance(bytes.toByteArray))
      res.right.get
    } else {
      null
    }
  }

  def unapplyMessage(message: ApiMessage): ByteString = {
    ByteString.copyFrom(message.toByteArray)
  }

  private def applyPeer(buf: ByteString): Peer =
    Peer.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get

  private def unapplyPeer(peer: Peer): ByteString =
    ByteString.copyFrom(peer.toByteArray)

  private def applyDateTime(millis: Long): DateTime = new DateTime(millis)

  private def unapplyDateTime(dt: DateTime): Long = dt.getMillis

  private def applyAvatar(buf: ByteString): Avatar =
    Avatar.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get

  private def unapplyAvatar(avatar: Avatar): ByteString =
    ByteString.copyFrom(avatar.toByteArray)

  private def applySex(i: Int): Sex = i match {
    case 1 ⇒ S.Unknown
    case 2 ⇒ S.Male
    case 3 ⇒ S.Female
  }

  private def unapplySex(sex: Sex): Int = sex.id

  implicit val messageMapper: TypeMapper[ByteString, ApiMessage] = TypeMapper(applyMessage)(unapplyMessage)

  implicit val peerMapper: TypeMapper[ByteString, Peer] = TypeMapper(applyPeer)(unapplyPeer)

  implicit val dateTimeMapper: TypeMapper[Long, DateTime] = TypeMapper(applyDateTime)(unapplyDateTime)

  implicit val avatarMapper: TypeMapper[ByteString, Avatar] = TypeMapper(applyAvatar)(unapplyAvatar)

  implicit val sexMapper: TypeMapper[Int, Sex] = TypeMapper(applySex)(unapplySex)
}
