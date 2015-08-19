/*package im.actor.server.sequence

import akka.persistence.{ AtLeastOnceDelivery, PersistentActor }

trait SeqUpdatesWriter {

}

class SeqUpdatesWriterActor(writerId: String) extends PersistentActor with AtLeastOnceDelivery {
  override def persistenceId: String = s"seq_upd_writer_${writerId}"

  override def receiveCommand: Receive = {

  }

  override def receiveRecover: Receive = {

  }
}
*/ 