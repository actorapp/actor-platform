package im.actor.server.persist.sequence

import im.actor.server.models
import scodec.bits.BitVector
import slick.driver.PostgresDriver.api._

object RefColumnType {
  implicit val refColumnType =
    MappedColumnType.base[models.sequence.Ref, Array[Byte]](
      refToByteArray, refFromByteArray)

  private def refToByteArray(ref: models.sequence.Ref): Array[Byte] = {
    models.sequence.RefCodec.encode(ref).toOption match {
      case Some(bytes) => bytes.toByteArray
      case None => throw new RuntimeException("failed to encode ref")
    }
  }

  private def refFromByteArray(bytes: Array[Byte]): models.sequence.Ref =
    models.sequence.RefCodec.decode(BitVector(bytes)).require.value
}
