/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';
import { ActionTypes } from '../constants/ActorAppConstants';
import ContactActionCreators from './ContactActionCreators';
import ComposeActionCreators from '../actions/ComposeActionCreators';
import history from '../utils/history';
import PeerUtils from '../utils/PeerUtils';

export default {
  open() {
    dispatch(ActionTypes.ABOUT_MODAL_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  close() {
    dispatch(ActionTypes.ABOUT_MODAL_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }
};
