/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch, dispatchAsync } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';
import ActorClient from 'utils/ActorClient';

export default {
  show: (gid) => {
    dispatch(ActionTypes.GROUP_EDIT_MODAL_SHOW, { gid });
  },

  hide: () => dispatch(ActionTypes.GROUP_EDIT_MODAL_HIDE),

  editGroupTitle: (gid, title) => {
    dispatchAsync(ActorClient.editGroupTitle(gid, title), {
      request: ActionTypes.GROUP_EDIT_TITLE,
      success: ActionTypes.GROUP_EDIT_TITLE_SUCCESS,
      failure: ActionTypes.GROUP_EDIT_TITLE_ERROR
    }, { gid, title });
  }
};
