/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { dispatch } from 'dispatcher/ActorAppDispatcher';
import { ActionTypes } from 'constants/ActorAppConstants';

const DraftActionCreators = {
  loadDraft(peer) {
    dispatch(ActionTypes.DRAFT_LOAD, {
      peer
    });
  },

  saveDraft(draft, saveNow = false) {
    dispatch(ActionTypes.DRAFT_SAVE, {
      draft, saveNow
    });
  }
};

export default DraftActionCreators;
