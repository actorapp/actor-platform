/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';
import EditGroupStore from 'stores/EditGroupStore'

export default {
  show(gid) {
    const group = ActorClient.getGroup(gid);
    ActorClient.bindGroup(gid, this.onCurrentGroupChange);
    dispatch(ActionTypes.GROUP_EDIT_MODAL_SHOW, { group });
  },

  hide()  {
    const group = EditGroupStore.getGroup();
    ActorClient.unbindGroup(group.id, this.onCurrentGroupChange);
    dispatch(ActionTypes.GROUP_EDIT_MODAL_HIDE)
  },

  onCurrentGroupChange(group) {
    dispatch(ActionTypes.GROUP_INFO_CHANGED, { group })
  },

  editGroupTitle(gid, title) {
    dispatchAsync(ActorClient.editGroupTitle(gid, title), {
      request: ActionTypes.GROUP_EDIT_TITLE,
      success: ActionTypes.GROUP_EDIT_TITLE_SUCCESS,
      failure: ActionTypes.GROUP_EDIT_TITLE_ERROR
    }, { gid, title });
  },

  changeGroupAvatar(gid, avatar) {
    ActorClient.changeGroupAvatar(gid, avatar)
  },

  editGroupAbout: (gid, about) => {
    dispatchAsync(ActorClient.editGroupAbout(gid, about), {
      request: ActionTypes.GROUP_EDIT_ABOUT,
      success: ActionTypes.GROUP_EDIT_ABOUT_SUCCESS,
      failure: ActionTypes.GROUP_EDIT_ABOUT_ERROR
    }, { gid, about });
  },

  removeGroupAvatar(gid) {
    ActorClient.removeGroupAvatar(gid)
  }
};
