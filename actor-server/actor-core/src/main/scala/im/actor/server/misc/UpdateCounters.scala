package im.actor.server.misc

import im.actor.api.rpc.counters.{ ApiAppCounters, UpdateCountersChanged }
import im.actor.server.persist
import slick.dbio._

import scala.concurrent.ExecutionContext

trait UpdateCounters {
  protected def getUpdateCountersChanged(userId: Int)(implicit ec: ExecutionContext): DBIO[UpdateCountersChanged] = for {
    unreadTotal ← persist.HistoryMessage.getUnreadTotal(userId)
  } yield UpdateCountersChanged(ApiAppCounters(Some(unreadTotal)))
}
