package im.actor

private[actor] trait Versioning {
  protected def getVersion: String = {
    val buildNumber = getBuildNumber

    if (buildNumber.contains(".")) { // already set by previous commands
      buildNumber
    } else {
      val version = s"1.0.$buildNumber$getVersionPostfix"

      if (sys.env.isDefinedAt("TEAMCITY_VERSION")) {
        println(s"##teamcity[buildNumber '$version']")
      }

      version
    }
  }

  private def getBuildNumber: String =
    sys.env.get("VERSION_PATCH")
      .orElse(sys.env.get("BUILD_NUMBER"))
      .getOrElse("0")

  private def getVersionPostfix: String = sys.env.get("VERSION_POSTFIX").map("-" + _).getOrElse("")
}