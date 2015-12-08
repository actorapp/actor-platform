package im.actor.server.model.configs

@SerialVersionUID(1L)
case class Parameter(userId: Int, key: String, value: Option[String])

object Parameter {
  object Keys {
    object Privacy {
      val LastSeen = "privacy.last_seen"
    }
  }

  sealed trait ParameterValue {
    def value: String
  }

  object Values {
    object Privacy {
      sealed trait LastSeen extends ParameterValue

      object LastSeen {
        object Always extends LastSeen {
          override def value: String = "always"
        }
        object Contacts extends LastSeen {
          override def value: String = "contacts"
        }
        object None extends LastSeen {
          override def value: String = "none"
        }
      }
    }
  }
}