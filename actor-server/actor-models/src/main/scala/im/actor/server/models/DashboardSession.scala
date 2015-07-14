package im.actor.server.models

import java.time.LocalDateTime

case class DashboardSession(id: Long, userId: Int, passcode: String, authToken: String, isActive: Boolean, createdAt: LocalDateTime)