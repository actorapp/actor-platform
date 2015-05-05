package im.actor.server.models

@SerialVersionUID(1L)
case class Manager(id: Int, name: String, lastName: String, domain: String, authToken: String, email: String)