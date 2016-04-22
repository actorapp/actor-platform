/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */


import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  show(group, prevModal) {
    ComposeActionCreators.toggleAutoFocus(false);
    ActorClient.getInviteUrl(group.id).then((url) => {
      dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW, { group, url, prevModal });
      ComposeActionCreators.toggleAutoFocus(false);
    }).catch((e) => {
      // TODO: handle error
      console.error(e);
    });
  },

  hide() {
    dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE);
    // ComposeActionCreators.toggleAutoFocus(true);
  }
};
