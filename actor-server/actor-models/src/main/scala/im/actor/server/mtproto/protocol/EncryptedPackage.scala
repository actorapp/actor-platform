package im.actor.server.mtproto.protocol

import scodec.bits.BitVector

// Wraps EncryptionCBCPackage and being embedded to Package after encoding
@SerialVersionUID(1L)
final case class EncryptedPackage(seq: Long, encryptedMessageBytes: BitVector) {
  val header = EncryptedPackage.header
}

object EncryptedPackage {
  val header = 0xE8
}

// Wraps encoded and encrypted MessageBox and being embedded to EncryptedPackage
@SerialVersionUID(1L)
final case class EncryptionCBCPackage(iv: BitVector, encryptedContent: BitVector)
