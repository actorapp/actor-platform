/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import ActorClient from '../utils/ActorClient';

import { ActionTypes } from '../constants/ActorAppConstants';
import DialogActionCreators from './DialogActionCreators';
import ComposeActionCreators from '../actions/ComposeActionCreators';

const CreateGroupActionCreators = {
  open() {
    dispatch(ActionTypes.GROUP_CREATE_MODAL_OPEN);
    ComposeActionCreators.toggleAutoFocus(false);
  },

  close() {
    dispatch(ActionTypes.GROUP_CREATE_MODAL_CLOSE);
    ComposeActionCreators.toggleAutoFocus(true);
  },

  setGroupName(name) {
    dispatch(ActionTypes.GROUP_CREATE_SET_NAME, { name });
  },

  //setGroupAvatar(avatar) {
  //  dispatch(ActionTypes.GROUP_CREATE_SET_AVATAR, { avatar });
  //},

  setSelectedUserIds(selectedUserIds) {
    dispatch(ActionTypes.GROUP_CREATE_SET_MEMBERS, { selectedUserIds });
  },

  createGroup(title, avatar, memberIds) {
    const createGroup = () => dispatchAsync(ActorClient.createGroup(title, avatar, memberIds), {
      request: ActionTypes.GROUP_CREATE,
      success: ActionTypes.GROUP_CREATE_SUCCESS,
      failure: ActionTypes.GROUP_CREATE_ERROR
    }, { title, avatar, memberIds });

    const openCreatedGroup = (peer) => DialogActionCreators.selectDialogPeer(peer);

    createGroup()
      .then(openCreatedGroup)
      .then(this.close)
  }
};

export default CreateGroupActionCreators;
