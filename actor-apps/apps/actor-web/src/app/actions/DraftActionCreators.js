import ActorAppDispatcher from '../dispatcher/ActorAppDispatcher';
import { ActionTypes } from '../constants/ActorAppConstants';

const DraftActionCreators = {
  loadDraft(peer) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DRAFT_LOAD,
      peer: peer
    });
  },

  saveDraft(draft) {
    ActorAppDispatcher.dispatch({
      type: ActionTypes.DRAFT_SAVE,
      draft: draft
    });
  }
};

export default DraftActionCreators;
