/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { defaultsDeep } from 'lodash';

/**
 * Class representing a delegate for overriding default app behaviour.
 *
 * @param {object} options - Object contains options.
 * @param {object} options.components - Object contains custom react components.
 * @param {object} options.features - Object contains features flags.
 * @param {object} options.actions - Object contains custom actions.
 * @param {object} options.l18n - Object contains custom translations.
 */
class ActorSDKDelegate {
  static defaultOptions = {
    components: {
      login: null,
      install: null,
      deactivated: null,
      join: null,
      archive: null,
      empty: null,
      sidebar: null,
      modals: null,
      about: null
    },
    features: {
      calls: true,
      search: false,
      editing: false,
      blocking: false,
      writeButton: false
    },
    actions: {
      setLoggedIn: null,
      setLoggedOut: null
    },
    l18n: {}
  };

  constructor(options = {}) {
    if (arguments.length === 3) {
      console.error('Deprecation notice: ActorSDKDelegate constructor accept "options" parameter');
      options = {
        components: arguments[0],
        actions: arguments[1],
        l18n: arguments[2]
      };
    }

    defaultsDeep(this, options, ActorSDKDelegate.defaultOptions);
  }
}

export default ActorSDKDelegate;
