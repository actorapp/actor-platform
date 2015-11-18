/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import Bugsnag from 'bugsnag-js';

export function initBugsnag(apikey) {
  Bugsnag.apiKey = apikey;
  Bugsnag.releaseStage = process.env.NODE_ENV;
}

export default Bugsnag;
