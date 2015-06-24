import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DraftActionCreators = {
  loadDraft(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DRAFT_LOAD,
      peer: peer
    });
  },

  saveDraft(draft, saveNow = false) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DRAFT_SAVE,
      draft: draft,
      saveNow: saveNow
    });
  }
};

export default DraftActionCreators;
