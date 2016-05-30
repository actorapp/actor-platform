/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import ActorSDK from '../src/sdk/actor-sdk';
import ActorSDKDelegate from '../src/sdk/actor-sdk-delegate';

const delegate = new ActorSDKDelegate({
  components: {},
  features: {
    calls: true,
    search: true,
    editing: true,
    blocking: true,
    writeButton: true
  },
  actions: {},
  l18n: {}
});

const app = new ActorSDK({
  delegate,
  endpoints: [
    'ws://220.189.207.18:9080'
  ],
  isExperimental: true,
  facebook: 'actorapp',
  twitter: 'actorapp',
  homePage: 'https://actor.im'
});

app.startApp();
