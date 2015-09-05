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
    dispatch(ActionTypes.CREATE_GROUP_MODAL_OPEN)
  },

  closeModal() {
    dispatch(ActionTypes.CREATE_GROUP_MODAL_CLOSE);
  },

  createGroup(title, avatar, memberIds) {
    const createGroup = () => dispatchAsync(ActorClient.createGroup(title, avatar, memberIds), {
      request: ActionTypes.CREATE_GROUP,
      success: ActionTypes.CREATE_GROUP_SUCCESS,
      failure: ActionTypes.CREATE_GROUP_ERROR
    }, { title, avatar, memberIds });

    createGroup().then((peer) => {
      this.closeModal();
      DialogActionCreators.selectDialogPeer(peer);
      mixpanel.track('Create group');
    });
  }
};

export default CreateGroupActionCreators;
