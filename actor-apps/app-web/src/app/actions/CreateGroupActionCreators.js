/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import ActorClient from 'utils/ActorClient';
import mixpanel from 'utils/Mixpanel';

import { ActionTypes } from 'constants/ActorAppConstants';

import DialogActionCreators from 'actions/DialogActionCreators';
import { dispatch } from 'dispatcher/ActorAppDispatcher';

const CreateGroupActionCreators = {
  openModal() {
    dispatch(ActionTypes.CREATE_GROUP_MODAL_OPEN)
  },

  closeModal() {
    dispatch(ActionTypes.CREATE_GROUP_MODAL_CLOSE);
  },

  createGroup(title, avatar, memberIds) {
    const p = ActorClient.createGroup(title, avatar, memberIds);

    p.then(
        peer => {
        DialogActionCreators.selectDialogPeer(peer);
        this.closeModal();
        mixpanel.track('Create group');
      },
        error => {
        console.error('Failed to create group', error);
      });

    return p;
  }
};

export default CreateGroupActionCreators;
