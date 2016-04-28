/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { dispatch } from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

import ActorClient from '../utils/ActorClient';

import DraftStore from '../stores/DraftStore';

export default {
  loadDraft(peer) {
    if (!peer) return null;

    const draft = ActorClient.loadDraft(peer);
    dispatch(ActionTypes.DRAFT_LOAD, { draft });
  },

  saveDraft(peer) {
    if (!peer) return null;

    const draft = DraftStore.getDraft();
    ActorClient.saveDraft(peer, draft);
    dispatch(ActionTypes.DRAFT_SAVE, { draft });
  },

  changeDraft(draft) {
    dispatch(ActionTypes.DRAFT_CHANGE, { draft });
  }
};
