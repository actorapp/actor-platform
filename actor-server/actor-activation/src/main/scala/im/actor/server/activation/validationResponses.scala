package im.actor.server.activation

sealed trait ValidationResponse

case object Validated extends ValidationResponse
case object ExpiredCode extends ValidationResponse
case object InvalidCode extends ValidationResponse
case object InvalidHash extends ValidationResponse
case object InvalidResponse extends ValidationResponse