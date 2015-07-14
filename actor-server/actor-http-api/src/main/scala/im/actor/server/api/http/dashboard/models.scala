package im.actor.server.api.http.dashboard

import im.actor.server.models

case class UserForm(userName: String, phone: Option[Long], email: Option[String])

case class UpdateForm(userName: String)

case class CompleteUser(user: models.User, phones: Seq[models.UserPhone], emails: Seq[models.UserEmail])

case class DashboardError(message: String)

case class CreatedUser(id: Int)

case class LoginForm(email: String, passcode: String)

case class AuthToken(authToken: String)