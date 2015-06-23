import Reflux from 'reflux';

import ActorClient from '../utils/ActorClient';

import DraftActions from '../actions/DraftActions';

let _draft = '';

let DraftStore = Reflux.createStore({
  init() {
    this.listenTo(DraftActions.saveDraft, this.saveDraft);
    this.listenTo(DraftActions.loadDraft, this.loadDraft);
  },

  getDraft() {
    return _draft;
  },

  saveDraft(peer, draft) {
    ActorClient.saveDraft(peer, draft);
  },

  loadDraft(peer) {
    _draft = ActorClient.loadDraft(peer);

    this.trigger();
  }
});

export default DraftStore;
