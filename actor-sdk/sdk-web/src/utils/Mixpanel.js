/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import mixpanel from 'mixpanel-browser/build/mixpanel.umd';

export function initMixpanel(apiKey) {
  mixpanel.init(apiKey);
}

export default mixpanel;
