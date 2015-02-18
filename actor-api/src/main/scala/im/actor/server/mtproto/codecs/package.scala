package im.actor.server.mtproto

package object codecs {
  val varint = VarIntCodec
  val bytes = BytesCodec
  val string = StringCodec
  val longs = LongsCodec
  val boolean = BooleanCodec
}
