package im.actor

import sbt.Keys._
import sbt._

private[actor] trait StartHook {
  lazy val startUpSettings = Seq(
    onLoad in Global := { state =>

      println("============== hello world")
      state
    }
  )

}
