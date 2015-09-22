/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from 'utils/ActorClient';
import mixpanel from 'utils/Mixpanel';

import { ActionTypes } from 'constants/ActorAppConstants';

import DialogActionCreators from 'actions/DialogActionCreators';
import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';

const CreateGroupActionCreators = {
  openModal() {
    dispatch(ActionTypes.GROUP_CREATE_MODAL_OPEN)
  },

  closeModal() {
    dispatch(ActionTypes.GROUP_CREATE_MODAL_CLOSE);
  },

  createGroup(title, avatar, memberIds) {
    const createGroup = () => dispatchAsync(ActorClient.createGroup(title, avatar, memberIds), {
      request: ActionTypes.GROUP_CREATE,
      success: ActionTypes.GROUP_CREATE_SUCCESS,
      failure: ActionTypes.GROUP_CREATE_ERROR
    }, { title, avatar, memberIds });

    createGroup().then((peer) => {
      this.closeModal();
      DialogActionCreators.selectDialogPeer(peer);
      mixpanel.track('Create group');
    });
  }
};

export default CreateGroupActionCreators;
