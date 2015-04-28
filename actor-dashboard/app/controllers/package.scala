import im.actor.server.models
import play.api.i18n.Lang

package object controllers {

  type Lang2UserAndPhone = Option[Lang] ⇒ (Option[models.User], Option[models.UserPhone])

}
