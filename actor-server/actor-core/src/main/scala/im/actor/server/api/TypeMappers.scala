package im.actor.server.api

import com.google.protobuf.{ ByteString, CodedInputStream }
import com.trueaccord.scalapb.TypeMapper
import im.actor.api.rpc.files.ApiAvatar
import im.actor.api.rpc.groups.ApiGroup
import im.actor.api.rpc.messaging.ApiMessage
import im.actor.api.rpc.misc.ApiExtension
import im.actor.api.rpc.peers.ApiPeer
import im.actor.api.rpc.sequence.SeqUpdate
import im.actor.api.rpc.users.ApiSex.ApiSex
import im.actor.api.rpc.users.{ ApiSex ⇒ S, ApiUser }
import im.actor.serialization.ActorSerializer
import org.joda.time.DateTime

object TypeMappers extends MessageMapper

private[api] trait MessageMapper {
  def get[E, A](xor: Either[E, A]): A = xor match {
    case Right(res) ⇒ res
    case Left(e)    ⇒ throw new Exception(s"Parse error: ${e}")
  }

  private def applyMessage(bytes: ByteString): ApiMessage = {
    if (bytes.size() > 0) {
      val res = ApiMessage.parseFrom(CodedInputStream.newInstance(bytes.toByteArray))
      get(res)
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
      get(res)
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
      get(res)
    } else {
      null
    }
  }

  private def unapplyGroup(group: ApiGroup): ByteString = {
    ByteString.copyFrom(group.toByteArray)
  }

  private def applyPeer(bytes: ByteString): ApiPeer = {
    if (bytes.size() > 0) {
      get(ApiPeer.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())))
    } else {
      null
    }
  }

  private def unapplyPeer(peer: ApiPeer): ByteString =
    ByteString.copyFrom(peer.toByteArray)

  private def applyDateTime(millis: Long): DateTime = new DateTime(millis)

  private def unapplyDateTime(dt: DateTime): Long = dt.getMillis

  private def applyAvatar(buf: ByteString): ApiAvatar =
    get(ApiAvatar.parseFrom(CodedInputStream.newInstance(buf.asReadOnlyByteBuffer())))

  private def unapplyAvatar(avatar: ApiAvatar): ByteString =
    ByteString.copyFrom(avatar.toByteArray)

  private def applySex(i: Int): ApiSex = i match {
    case 2 ⇒ S.Male
    case 3 ⇒ S.Female
    case _ ⇒ S.Unknown
  }

  private def unapplySex(sex: ApiSex): Int = sex.id

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
      get(SeqUpdate.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())))
    } else {
      null
    }
  }

  def unapplySeqUpdate(upd: SeqUpdate): ByteString = {
    ByteString.copyFrom(upd.toByteArray)
  }

  def applyExtension(bytes: ByteString): ApiExtension = {
    if (bytes.size > 0) {
      get(ApiExtension.parseFrom(CodedInputStream.newInstance(bytes.asReadOnlyByteBuffer())))
    } else {
      null
    }
  }

  def unapplyExtension(ext: ApiExtension): ByteString =
    ByteString.copyFrom(ext.toByteArray)

  implicit val seqUpdMapper: TypeMapper[ByteString, SeqUpdate] = TypeMapper(applySeqUpdate)(unapplySeqUpdate)

  implicit val anyRefMapper: TypeMapper[ByteString, AnyRef] = TypeMapper(applyAnyRef)(unapplyAnyRef)

  implicit val messageMapper: TypeMapper[ByteString, ApiMessage] = TypeMapper(applyMessage)(unapplyMessage)

  implicit val userMapper: TypeMapper[ByteString, ApiUser] = TypeMapper(applyUser)(unapplyUser)

  implicit val groupMapper: TypeMapper[ByteString, ApiGroup] = TypeMapper(applyGroup)(unapplyGroup)

  implicit val peerMapper: TypeMapper[ByteString, ApiPeer] = TypeMapper(applyPeer)(unapplyPeer)

  implicit val dateTimeMapper: TypeMapper[Long, DateTime] = TypeMapper(applyDateTime)(unapplyDateTime)

  implicit val avatarMapper: TypeMapper[ByteString, ApiAvatar] = TypeMapper(applyAvatar)(unapplyAvatar)

  implicit val sexMapper: TypeMapper[Int, ApiSex] = TypeMapper(applySex)(unapplySex)

  implicit val extensionMapper: TypeMapper[ByteString, ApiExtension] = TypeMapper(applyExtension)(unapplyExtension)
}

