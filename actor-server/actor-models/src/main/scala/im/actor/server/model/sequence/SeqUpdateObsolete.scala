package im.actor.server.model.sequence

case class SeqUpdateObsolete(authId: Long, timestamp: Long, seq: Int, header: Int, serializedData: Array[Byte], userIds: Set[Int], groupIds: Set[Int])

