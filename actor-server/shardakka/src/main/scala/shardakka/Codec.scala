package shardakka

import com.google.protobuf.ByteString

abstract class Codec[A] extends Encoder[A] with Decoder[A]

trait Encoder[A] {
  def toString(bytes: ByteString): String

  def toBytes(value: A): ByteString
}

trait Decoder[A] {
  def fromBytes(bytes: ByteString): A
}

final object StringCodec extends Codec[String] {
  override def toString(bytes: ByteString): String = bytes.toStringUtf8

  override def toBytes(value: String): ByteString = ByteString.copyFromUtf8(value)

  override def fromBytes(bytes: ByteString): String = bytes.toStringUtf8
}
