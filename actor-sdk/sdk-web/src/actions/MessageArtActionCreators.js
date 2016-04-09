/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from './ComposeActionCreators';

class MessageArtActionCreators {
  open() {
    dispatch(ActionTypes.MESSAGE_ART_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  }

  close() {
    dispatch(ActionTypes.MESSAGE_ART_CLOSE);
    ComposeActionCreators.toggleAutoFocus(true);
  }
}

export default new MessageArtActionCreators();
