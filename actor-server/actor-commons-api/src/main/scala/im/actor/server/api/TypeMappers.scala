package im.actor.server.api

import com.google.protobuf.{ ByteString, CodedInputStream }
import com.trueaccord.scalapb.TypeMapper
import im.actor.api.rpc.misc.Extension
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.api.rpc.users.{ Sex ⇒ S }
import im.actor.api.rpc.users.Sex.Sex
import org.joda.time.DateTime

import im.actor.api.rpc.groups.{ Group ⇒ ApiGroup }
import im.actor.api.rpc.files.Avatar
import im.actor.api.rpc.messaging.{ Message ⇒ ApiMessage }
import im.actor.api.rpc.peers.Peer
import im.actor.api.rpc.users.{ User ⇒ ApiUser }
import im.actor.server.commons.serialization.ActorSerializer

object TypeMappers extends MessageMapper

private[api] trait MessageMapper {
  private def applyMessage(bytes: ByteString): ApiMessage = {
    if (bytes.size() > 0) {
      val res = ApiMessage.parseFrom(CodedInputStream.newInstance(bytes.toByteArray))
      res.right.get
    } else {
      null
    }
  }

  private def unapplyMessage(message: ApiMessage): ByteString = {
    ByteString.copyFrom(message.toByteArray)
  }

  private def applyUser(bytes: ByteString): ApiUser = {
    if (bytes.size() > 0) {
      val res = ApiUser.parseFrom(CodedInputStream.newInstance(bytes.toByteArray))
      res.right.get
    } else {
      null
    }
  }

  private def unapplyUser(user: ApiUser): ByteString = {
    ByteString.copyFrom(user.toByteArray)
  }

  private def applyGroup(bytes: ByteString): ApiGroup = {
    if (bytes.size() > 0) {
      val res = ApiGroup.parseFrom(CodedInputStream.newInstance(bytes.toByteArray))
      res.right.get
    } else {
      null
    }
  }

  private def unapplyGroup(group: ApiGroup): ByteString = {
    ByteString.copyFrom(group.toByteArray)
  }

  private def applyPeer(bytes: ByteString): Peer = {
    if (bytes.size() > 0) {
      Peer.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())).right.get
    } else {
      null
    }
  }

  private def unapplyPeer(peer: Peer): ByteString =
    ByteString.copyFrom(peer.toByteArray)

  private def applyDateTime(millis: Long): DateTime = new DateTime(millis)

  private def unapplyDateTime(dt: DateTime): Long = dt.getMillis

  private def applyAvatar(buf: ByteString): Avatar =
    Avatar.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())).right.get

  private def unapplyAvatar(avatar: Avatar): ByteString =
    ByteString.copyFrom(avatar.toByteArray)

  private def applySex(i: Int): Sex = i match {
    case 2 ⇒ S.Male
    case 3 ⇒ S.Female
    case _ ⇒ S.Unknown
  }

  private def unapplySex(sex: Sex): Int = sex.id

  def applyAnyRef(buf: ByteString): AnyRef = {
    if (buf.size() > 0) {
      ActorSerializer.fromBinary(buf.toByteArray)
    } else {
      null
    }
  }

  def unapplyAnyRef(msg: AnyRef): ByteString = {
    ByteString.copyFrom(ActorSerializer.toBinary(msg))
  }

  def applySeqUpdate(bytes: ByteString): SeqUpdate = {
    if (bytes.size() > 0) {
      SeqUpdate.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())).right.get
    } else {
      null
    }
  }

  def unapplySeqUpdate(upd: SeqUpdate): ByteString = {
    ByteString.copyFrom(upd.toByteArray)
  }

  def applyExtension(bytes: ByteString): Extension = {
    if (bytes.size > 0) {
      Extension.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())).right.get
    } else {
      null
    }
  }

  def unapplyExtension(ext: Extension): ByteString =
    ByteString.copyFrom(ext.toByteArray)

  implicit val seqUpdMapper: TypeMapper[ByteString, SeqUpdate] = TypeMapper(applySeqUpdate)(unapplySeqUpdate)

  implicit val anyRefMapper: TypeMapper[ByteString, AnyRef] = TypeMapper(applyAnyRef)(unapplyAnyRef)

  implicit val messageMapper: TypeMapper[ByteString, ApiMessage] = TypeMapper(applyMessage)(unapplyMessage)

  implicit val userMapper: TypeMapper[ByteString, ApiUser] = TypeMapper(applyUser)(unapplyUser)

  implicit val groupMapper: TypeMapper[ByteString, ApiGroup] = TypeMapper(applyGroup)(unapplyGroup)

  implicit val peerMapper: TypeMapper[ByteString, Peer] = TypeMapper(applyPeer)(unapplyPeer)

  implicit val dateTimeMapper: TypeMapper[Long, DateTime] = TypeMapper(applyDateTime)(unapplyDateTime)

  implicit val avatarMapper: TypeMapper[ByteString, Avatar] = TypeMapper(applyAvatar)(unapplyAvatar)

  implicit val sexMapper: TypeMapper[Int, Sex] = TypeMapper(applySex)(unapplySex)

  implicit val extensionMapper: TypeMapper[ByteString, Extension] = TypeMapper(applyExtension)(unapplyExtension)
}

object CommonSerialization {
  def register(): Unit = {
    ActorSerializer.register(100, classOf[im.actor.server.event.TSEvent])
  }
}