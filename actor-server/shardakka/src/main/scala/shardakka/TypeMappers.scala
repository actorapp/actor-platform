package shardakka

import java.io._

import com.eaio.uuid.UUID
import com.google.protobuf.ByteString
import com.trueaccord.scalapb.TypeMapper

object TypeMappers extends UUIDMapper

private[shardakka] trait UUIDMapper {
  private def applyUUID(bytes: ByteString): UUID = {
    val bis = bytes.newInput()
    val ois = new ObjectInputStream(bis)

    try {
      val uuid = UUID.nilUUID()
      uuid.readExternal(ois)
      uuid
    } finally {
      ois.close()
      bis.close()
    }
  }

  private def unapplyUUID(uuid: UUID): ByteString = {
    val bos = new ByteArrayOutputStream(2)
    val oos = new ObjectOutputStream(bos)

    try {
      uuid.writeExternal(oos)
      ByteString.copyFrom(bos.toByteArray)
    } finally {
      oos.close()
      bos.close()
    }
  }

  implicit val uuidMapper: TypeMapper[ByteString, UUID] = TypeMapper[ByteString, UUID](applyUUID)(unapplyUUID)
}
