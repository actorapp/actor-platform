/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

class ActorSDKDelegate {
  constructor(components = {}) {
    this.loginComponent = components.loginComponent || null;
    this.recentComponent = components.recentComponent || null;
  }
}

export default ActorSDKDelegate;
