/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DialogInfoActionCreators = {
  setDialogInfo(info) {
    dispatch(ActionTypes.DIALOG_INFO_CHANGED, { info });
  }
};

export default DialogInfoActionCreators;
