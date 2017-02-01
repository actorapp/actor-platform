package im.actor.server.grouppre

import java.time.Instant

import akka.persistence.SnapshotMetadata
import im.actor.server.cqrs.{Event, ProcessorState}

/**
  * Created by 98379720172 on 31/01/17.
  */
private[grouppre] final case class GroupPreState(
  groupId:Int,
  groupFatherId:Int,
  createdAt:Option[Instant],
  creatorUserId: Int
) extends ProcessorState[GroupPreState]{

  override def updated(e: Event): GroupPreState = ???

  override def withSnapshot(metadata: SnapshotMetadata, snapshot: Any): GroupPreState = ???
}
