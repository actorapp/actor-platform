/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DraftActionCreators = {
  setTyping(typing) {
    dispatch(ActionTypes.TYPING_CHANGED, { typing: typing.typing });
  }
};

export default DraftActionCreators;
