/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';

import EditGroupStore from '../stores/EditGroupStore'

import ActionCreators from './ActionCreators';
import ComposeActionCreators from './ComposeActionCreators';


class EditGroupActionCreators extends ActionCreators {
  show(gid) {
    this.setBindings('group', [
      ActorClient.bindGroup(gid, this.onCurrentGroupChange)
    ]);

    const group = ActorClient.getGroup(gid);
    dispatch(ActionTypes.GROUP_EDIT_MODAL_SHOW, { group });

    ComposeActionCreators.toggleAutoFocus(false);
  }

  hide() {
    this.removeBindings('group');

    dispatch(ActionTypes.GROUP_EDIT_MODAL_HIDE);

    ComposeActionCreators.toggleAutoFocus(true);
  }

  onCurrentGroupChange(group) {
    dispatch(ActionTypes.GROUP_INFO_CHANGED, { group })
  }

  editGroupTitle(gid, title) {
    if (title !== EditGroupStore.getTitle()) {
      dispatchAsync(ActorClient.editGroupTitle(gid, title), {
        request: ActionTypes.GROUP_EDIT_TITLE,
        success: ActionTypes.GROUP_EDIT_TITLE_SUCCESS,
        failure: ActionTypes.GROUP_EDIT_TITLE_ERROR
      }, { gid, title });
    }
  }

  changeGroupAvatar(gid, avatar) {
    ActorClient.changeGroupAvatar(gid, avatar)
  }

  editGroupAbout(gid, about) {
    about = about === '' ? null : about;
    if (about !== EditGroupStore.getAbout()) {
      dispatchAsync(ActorClient.editGroupAbout(gid, about), {
        request: ActionTypes.GROUP_EDIT_ABOUT,
        success: ActionTypes.GROUP_EDIT_ABOUT_SUCCESS,
        failure: ActionTypes.GROUP_EDIT_ABOUT_ERROR
      }, { gid, about });
    }
  }

  removeGroupAvatar(gid) {
    ActorClient.removeGroupAvatar(gid)
  }
}

export default new EditGroupActionCreators();
