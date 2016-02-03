/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

/**
 * Class representing a delegate for overriding default app behaviour.
 */
class ActorSDKDelegate {
  /**
   * @constructor
   * @param {object} components - Object contains custom react components.
   * @param {object} actions - Object contains custom actions.
   * @param {object} l18n - Object contains custom translations.
   */
  constructor(components = {}, actions = {}, l18n = {}) {
    this.components = {
      login: components.login || null,
      install: components.install || null,
      deactivated: components.deactivated || null,
      join: components.join || null,

      sidebar: components.sidebar || null,
      dialog: components.dialog || null
    };

    this.actions = {
      setLoggedIn: actions.setLoggedIn || null,
      setLoggedOut: actions.setLoggedOut || null
    };

    this.l18n = l18n;
  }
}

export default ActorSDKDelegate;
