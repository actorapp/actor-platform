/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import { debounce } from 'lodash';
import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';
import ActorClient from '../utils/ActorClient';
import DraftStore from '../stores/DraftStore';

export default {
  loadDraft(peer) {
    const draft = ActorClient.loadDraft(peer);
    dispatch(ActionTypes.DRAFT_LOAD, { draft });
  },

  saveDraft(peer) {
    const draft = DraftStore.getDraft();
    ActorClient.saveDraft(peer, draft);
    dispatch(ActionTypes.DRAFT_SAVE, { draft });
  },

  changeDraft: debounce((draft) => {
    dispatch(ActionTypes.DRAFT_CHANGE, { draft });
  }, 300, {trailing: true})
};
