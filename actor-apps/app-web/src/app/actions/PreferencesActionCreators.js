/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

export default {
  show: () => {
    dispatch(ActionTypes.PREFERENCES_MODAL_SHOW);
  },

  hide: () => {
    dispatch(ActionTypes.PREFERENCES_MODAL_HIDE);
  },

  load: () => {
    dispatch(ActionTypes.PREFERENCES_LOAD);
  },

  save: (preferences) => {
    dispatch(ActionTypes.PREFERENCES_SAVE, {
      preferences
    });
  }
};
