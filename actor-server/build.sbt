addCommandAlias("debianPackage", "debian:packageBin")
addCommandAlias("debianPackageSystemd",
  "; set serverLoading in Debian := com.typesafe.sbt.packager.archetypes.ServerLoader.Systemd ;debian:packageBin"
)

defaultLinuxInstallLocation in Docker := "/var/lib/actor"
