/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorSDK from '../src/sdk/actor-sdk';
import ActorSDKDelegate from '../src/sdk/actor-sdk-delegate';

const endpoints = [
  'wss://front1-ws-mtproto-api-rev2.actor.im',
  'wss://front2-ws-mtproto-api-rev2.actor.im'
];

const components = {};
const actions = {};
const l18n = {};

const delegate = new ActorSDKDelegate(components, actions, l18n);

const app = new ActorSDK({endpoints, delegate, isExperimental: true});
app.startApp();
