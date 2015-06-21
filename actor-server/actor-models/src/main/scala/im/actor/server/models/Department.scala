package im.actor.server.models

import com.github.tminglei.slickpg.LTree
import org.joda.time.DateTime

case class Department(id: Int, name: String, struct: LTree, deletedAt: Option[DateTime] = None)