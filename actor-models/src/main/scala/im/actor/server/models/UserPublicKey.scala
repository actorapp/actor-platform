package im.actor.server.models

import scodec.bits.BitVector

@SerialVersionUID(1L)
case class UserPublicKey(
  userId: Int,
  hash: Long,
  data: BitVector,
  authId: Long
)
