import Reflux from 'reflux';

import ActorClient from 'utils/ActorClient';

import InviteUserActions from 'actions/InviteUserActions';

let
  _isModalOpen = false,
  _group = null,
  _inviteUrl = null;

export default Reflux.createStore({
  init () {
    this.listenTo(InviteUserActions.modalOpen, this.onModalOpen);
    this.listenTo(InviteUserActions.modalClose, this.onModalClose);
  },

  isModalOpen () {
    return _isModalOpen;
  },

  getGroup () {
    return _group;
  },

  getInviteUrl () {
    return _inviteUrl;
  },

  onModalOpen (group) {
    _isModalOpen = true;
    _group = group;

    ActorClient.getInviteUrl(group.id)
      .then((url) => {
        _inviteUrl = url;
        this.trigger();
      });

    this.trigger();
  },

  onModalClose () {
    _isModalOpen = false;
    _group = null;

    this.trigger();
  }
});
