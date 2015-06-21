package im.actor.server.models.sequence

case class SeqUpdate(authId: Long, timestamp: Long, seq: Int, header: Int, serializedData: Array[Byte], userIds: Set[Int], groupIds: Set[Int])

