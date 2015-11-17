/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import mixpanel from 'mixpanel-browser/build/mixpanel.umd';
import { MixpanelAPIKey } from 'constants/ActorAppConstants';

mixpanel.init(MixpanelAPIKey);

export default mixpanel;
