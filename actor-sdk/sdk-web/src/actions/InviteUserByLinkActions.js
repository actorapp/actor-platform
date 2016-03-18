/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */


import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ComposeActionCreators from '../actions/ComposeActionCreators';
import ActorClient from '../utils/ActorClient';

export default {
  show(group) {
    ActorClient.getInviteUrl(group.id)
      .then((url) => {
        dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW, { group, url });
        ComposeActionCreators.toggleAutoFocus(false);
      });
  },

  hide() {
    dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  }
};
