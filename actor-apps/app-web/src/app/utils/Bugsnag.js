/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import Bugsnag from 'bugsnag-js';
import { BugsnagAPIKey } from 'constants/ActorAppConstants'

Bugsnag.apiKey = BugsnagAPIKey;

export default Bugsnag;
