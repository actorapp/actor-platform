package im.actor.server.dashboard

import play.api.i18n.Lang

import im.actor.server.models

package object controllers {

  type Lang2CompleteUser = Option[Lang] ⇒ Option[(models.User, String, models.UserPhone)]

}
