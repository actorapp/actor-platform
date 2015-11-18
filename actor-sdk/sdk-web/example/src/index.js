/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { ActorSDK, ActorSDKDelegate } from '../../index';
import config from './app.json';

const delegate = new ActorSDKDelegate();
const endpoints = config.endpoints;
const bugsnagApiKey = config.bugsnag;
const mixpanelAPIKey = config.mixpanel;

new ActorSDK({endpoints, delegate, bugsnagApiKey, mixpanelAPIKey}).startApp();
