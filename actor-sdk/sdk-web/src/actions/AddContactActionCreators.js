/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';
import { ActionTypes } from '../constants/ActorAppConstants';
import ContactActionCreators from './ContactActionCreators';
import DialogActionCreators from './DialogActionCreators';
import ComposeActionCreators from '../actions/ComposeActionCreators';

export default {
  open() {
    dispatch(ActionTypes.CONTACT_ADD_MODAL_SHOW);
    ComposeActionCreators.toggleAutoFocus(false);
  },


  close() {
    dispatch(ActionTypes.CONTACT_ADD_MODAL_HIDE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  findUsers(query) {
    dispatchAsync(ActorClient.findUsers(query), {
      request: ActionTypes.CONTACT_FIND,
      success: ActionTypes.CONTACT_FIND_SUCCESS,
      failure: ActionTypes.CONTACT_FIND_ERROR
    }, { query })
  },

  addToContacts(uid, isContact) {
    const peer = ActorClient.getUserPeer(uid);
    if (!isContact) {
      ContactActionCreators.addContact(uid);
    }
    DialogActionCreators.selectDialogPeer(peer);
  }
};
