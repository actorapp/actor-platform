package im.actor.server

trait ImplicitRegions
  extends ImplicitSeqUpdatesManagerRegion
  with ImplicitSessionRegion
  with ImplicitSocialManagerRegion

trait ImplicitServiceDependencies extends ImplicitFileStorageAdapter

