package im.actor.server.model

import scodec.bits._

case class PersistenceMessage(processorId: String, partitionNr: Long, sequenceNr: Long, marker: String, message: BitVector)
