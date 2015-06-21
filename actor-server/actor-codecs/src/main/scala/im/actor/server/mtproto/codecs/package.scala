package im.actor.server.mtproto

package object codecs {
  val byteSize = 8L
  val int32Bytes = 4
  val uint8Bytes = 1
  val longBytes = 8
  val int32Bits = byteSize * 4
  val longBits = byteSize * 8

  val varint = VarIntCodec
  val bytes = BytesCodec
  val string = StringCodec
  val longs = LongsCodec
  val boolean = BooleanCodec
}
