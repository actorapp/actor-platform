package im.actor.server.api.http.dashboard

import im.actor.server.models

case class CompleteUser(user: models.User, phones: Seq[models.UserPhone], emails: Seq[models.UserEmail])

case class DashboardError(message: String)

case class CreatedUserId(id: Int)

case class AuthToken(authToken: String)