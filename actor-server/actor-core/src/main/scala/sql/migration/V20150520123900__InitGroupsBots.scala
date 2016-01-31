package sql.migration

import java.sql.Connection

import com.typesafe.slick.testkit.util.DelegateConnection
import org.apache.commons.codec.digest.DigestUtils
import org.flywaydb.core.api.migration.jdbc.JdbcMigration
import slick.driver.PostgresDriver.api._
import slick.jdbc.JdbcDataSource

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

class V20150520123900__InitGroupsBots extends JdbcMigration {
  def migrate(connection: Connection): Unit = {
    val wrappedConn = new DelegateConnection(connection) {
      override def close(): Unit = ()
    }
    val db = Database.forSource(new JdbcDataSource {
      def createConnection(): Connection = wrappedConn
      def close(): Unit = ()
    })

    Await.ready(
      db.run {
        for {
          groupIds ← sql"select id from groups".as[Int]
          _ ← DBIO.sequence(groupIds.map { groupId ⇒
            val rnd = ThreadLocalRandom.current()
            val botId = nextIntId(rnd)
            val accessSalt = nextAccessSalt(rnd)
            val botToken = DigestUtils.sha256Hex(rnd.nextInt().toString)
            for {
              _ ← sqlu"""insert into users(id, access_salt, name, country_code, sex, state, deleted_at, is_bot)
                values($botId, $accessSalt, 'Bot', 'US', 1, 1, null, true)"""
              _ ← sqlu"insert into groups_bots(group_id, user_id, token) values($groupId, $botId, $botToken)"
            } yield ()
          })
        } yield ()
      }, 5.seconds
    )

  }

  private def nextIntId(rnd: ThreadLocalRandom): Int = rnd.nextInt(Int.MaxValue) + 1

  private def nextAccessSalt(rnd: ThreadLocalRandom): String = rnd.nextLong().toString
}
