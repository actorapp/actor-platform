package im.actor

private[actor] trait Versioning {
  protected def getVersion: String = {
    val version = s"1.0.$getVersionPatch$getVersionPostfix"

    if (sys.env.isDefinedAt("TEAMCITY_VERSION")) {
      println(s"##teamcity[buildNumber '$version']")
    }

    version
  }

  private def getVersionPatch: String = sys.env.get("VERSION_MINOR").orElse(sys.env.get("BUILD_NUMBER")).getOrElse("0")

  private def getVersionPostfix: String = sys.env.get("VERSION_POSTFIX").map("-" + _).getOrElse("")
}