/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */


import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show: (group) => {
    dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_SHOW, {
      group
    });
  },

  hide: () => {
    dispatch(ActionTypes.INVITE_USER_BY_LINK_MODAL_HIDE);
  }
};
