/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { EventTypes } from '../constants/ActorAppConstants';
import CallActionCreators from './CallActionCreators';

export default {
  broadcastEvent(type, event) {
    //console.debug('broadcastEvent', type, event);
    switch (type) {
      case EventTypes.CALLS:
        CallActionCreators.handleCall(event);
        break;
      default:
    }
  }
};
