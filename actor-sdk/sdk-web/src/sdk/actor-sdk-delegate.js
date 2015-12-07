/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

class ActorSDKDelegate {
  constructor(components = {}, actions = {}, l18n = {}) {
    console.debug('ActorSDKDelegate constructor:', components, actions, l18n);
    this.components = {
      login: components.login || null,
      recent: components.recent || null,
      toolbar: components.toolbar || null,
      activity: components.activity || null
    };

    this.actions = {
      setLoggedIn: actions.setLoggedIn || null,
      setLoggedOut: actions.setLoggedOut || null
    };

    this.l18n = l18n;
  }
}

export default ActorSDKDelegate;
