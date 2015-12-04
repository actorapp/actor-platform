/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

class ActorSDKDelegate {
  constructor(components = {}, actions = {}) {
    this.components = {
      login: components.login || null,
      recent: components.recent || null
    };

    this.actions = {
      setLoggedIn: actions.setLoggedIn || null,
      setLoggedOut: actions.setLoggedOut || null
    };
  }
}

export default ActorSDKDelegate;
