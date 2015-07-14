package im.actor.server.dashboard.models

case class UserEmail(email: String)

case class LoginForm(email: String, passcode: String)

case class UpdateForm(userName: String)

case class UserForm(userName: String, phone: Option[String], email: Option[String])