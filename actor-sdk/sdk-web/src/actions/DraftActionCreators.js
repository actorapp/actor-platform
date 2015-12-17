/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DraftActionCreators = {
  loadDraft(peer) {
    dispatch(ActionTypes.DRAFT_LOAD, {
      peer
    });
  },

  saveDraft: debounce((draft, saveNow = false) => {
    dispatch(ActionTypes.DRAFT_SAVE, { draft, saveNow });
  }, 300, {trailing: true})
};

export default DraftActionCreators;
