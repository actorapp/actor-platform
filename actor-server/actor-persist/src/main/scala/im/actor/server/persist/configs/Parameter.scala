package im.actor.server.persist.configs

import scala.concurrent.ExecutionContext

import slick.driver.PostgresDriver.api._

import im.actor.server.models

class ParameterTable(tag: Tag) extends Table[models.configs.Parameter](tag, "config_parameters") {
  def userId = column[Int]("user_id", O.PrimaryKey)

  def key = column[String]("key", O.PrimaryKey)

  def value = column[Option[String]]("value")

  def * = (userId, key, value) <> (models.configs.Parameter.tupled, models.configs.Parameter.unapply)
}

object Parameter {
  val parameters = TableQuery[ParameterTable]

  def createOrUpdate(parameter: models.configs.Parameter) =
    parameters.insertOrUpdate(parameter)

  def find(userId: Int) =
    parameters.filter(_.userId === userId).result

  private def byUserIdAndKey(userId: Rep[Int], key: Rep[String]) = parameters.filter(p ⇒ p.userId === userId && p.key === key).map(_.value)
  private val byUserIdAndKeyC = Compiled(byUserIdAndKey _)

  def findValue(userId: Int, key: String)(implicit ec: ExecutionContext) =
    byUserIdAndKeyC(userId → key).result.headOption map (_.flatten)
}