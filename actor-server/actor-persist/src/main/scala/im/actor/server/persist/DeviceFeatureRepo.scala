package im.actor.server.persist

import com.google.protobuf.ByteString
import im.actor.server.db.ActorPostgresDriver.api._
import im.actor.server.model.DeviceFeature

final class DeviceFeatureTable(tag: Tag) extends Table[DeviceFeature](tag, "device_features") {
  def authId = column[Long]("auth_id", O.PrimaryKey)

  def name = column[String]("name", O.PrimaryKey)

  def args = column[ByteString]("args")

  def * = (authId, name, args) <> ((DeviceFeature.apply _).tupled, DeviceFeature.unapply)
}

object DeviceFeatureRepo {
  val deviceFeatures = TableQuery[DeviceFeatureTable]

  def byPK(authId: Rep[Long], name: Rep[String]) = deviceFeatures filter (df ⇒ df.authId === authId && df.name === name)

  val byPKC = Compiled(byPK _)
  val existsC = Compiled { (authId: Rep[Long], name: Rep[String]) ⇒
    byPK(authId, name).exists
  }

  def enable(feature: DeviceFeature) =
    deviceFeatures.insertOrUpdate(feature)

  def disable(authId: Long, name: String) =
    byPKC((authId, name)).delete

  def find(authId: Long, name: String) =
    byPKC((authId, name)).result.headOption

  def exists(authId: Long, name: String) =
    existsC((authId, name)).result
}