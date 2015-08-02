package im.actor.server

trait ImplicitRegions
  extends ImplicitSeqUpdatesManagerRegion
  with ImplicitSessionRegionProxy
  with ImplicitSocialManagerRegion

trait ImplicitServiceDependencies extends ImplicitFileStorageAdapter

