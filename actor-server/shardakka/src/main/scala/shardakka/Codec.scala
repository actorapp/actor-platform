package shardakka

import java.nio.ByteBuffer
import java.time.Instant

import com.google.common.primitives.{ Ints, Longs }
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

object InstantCodec extends Codec[Instant] {
  override def toString(bytes: ByteString): String = fromBytes(bytes).toString

  override def toBytes(value: Instant): ByteString =
    ByteString.copyFrom(Longs.toByteArray(value.toEpochMilli))

  override def fromBytes(bytes: ByteString): Instant =
    Instant.ofEpochMilli(ByteBuffer.wrap(bytes.toByteArray).getLong)
}

object IntCodec extends Codec[Int] {
  override def toString(bytes: ByteString): String = fromBytes(bytes).toString

  override def toBytes(value: Int): ByteString = ByteString.copyFrom(Ints.toByteArray(value))

  override def fromBytes(bytes: ByteString): Int = ByteBuffer.wrap(bytes.toByteArray).getInt
}

object LongCodec extends Codec[Long] {
  override def toString(bytes: ByteString): String = fromBytes(bytes).toString

  override def toBytes(value: Long): ByteString = ByteString.copyFrom(Longs.toByteArray(value))

  override def fromBytes(bytes: ByteString): Long = ByteBuffer.wrap(bytes.toByteArray).getLong()
}