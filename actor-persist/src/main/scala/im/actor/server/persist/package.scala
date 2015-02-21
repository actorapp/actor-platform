package im.actor.server

import scodec.bits.BitVector
import slick.driver.PostgresDriver.simple._

package object persist {
  implicit val bitVectorColumnType =
    MappedColumnType.base[BitVector, Array[Byte]](_.toByteArray, BitVector(_))
}
