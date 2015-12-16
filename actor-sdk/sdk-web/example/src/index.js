/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ActorSDK, ActorSDKDelegate } from 'actor-sdk';

import ToolbarSection from './components/ToolbarSection.react';
import TextMessage from './components/dialog/messages/TextMessage.react';

const endpoints = [
  'wss://front1-ws-mtproto-api-rev2.actor.im',
  'wss://front2-ws-mtproto-api-rev2.actor.im'
];
const mixpanelAPIKey = '9591b090b987c2b701db5a8ef3e5055c';
const bugsnagApiKey = 'cd24ee53326e06669a36c637b29660c3';

const components = {
  dialog: {
    toolbar: ToolbarSection,
    messages: {
      text: TextMessage
    }
  }
};

const actions = {};
const l18n = {};

const delegate = new ActorSDKDelegate(components, actions, l18n);

const app = new ActorSDK({endpoints, delegate, bugsnagApiKey, mixpanelAPIKey});
app.startApp();
