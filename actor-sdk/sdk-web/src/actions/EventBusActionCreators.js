/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes, EventTypes, CallTypes } from '../constants/ActorAppConstants';
import CallActionCreators from './CallActionCreators';

const EventBusActionCreators = {
  broadcastEvent(type, event) {
    //console.debug('broadcastEvent', type, event);
    switch (type) {
      case EventTypes.CALL:
        CallActionCreators.handleCall(event);
        break;
      default:
    }
  }
};

export default EventBusActionCreators;
