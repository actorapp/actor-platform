/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ActorSDK, ActorSDKDelegate } from 'actor-sdk';
import config from './app.json';

import Login from './components/Login.react';

const endpoints = config.endpoints;
const bugsnagApiKey = config.bugsnag;
const mixpanelAPIKey = config.mixpanel;

const delegate = new ActorSDKDelegate({
  loginComponent: Login,
  sidebarComponent: null
});

new ActorSDK({endpoints, delegate, bugsnagApiKey, mixpanelAPIKey}).startApp();
