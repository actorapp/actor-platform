import Reflux from 'reflux';

import ActorClient from '../utils/ActorClient';

import MyProfileActions from '../actions/MyProfileActions';

let
  _profile = null,
  _isModalOpen = false;

export default Reflux.createStore({
  init () {
    this.listenTo(MyProfileActions.modalOpen, this.onModalOpen);
    this.listenTo(MyProfileActions.modalClose, this.onModalClose);
  },

  isModalOpen() {
    return _isModalOpen;
  },

  getProfile() {
    return _profile;
  },

  setProfile(profile) {
    _profile = profile;

    this.trigger();
  },

  onModalOpen() {
    ActorClient.bindUser(ActorClient.getUid(), this.setProfile);
    _isModalOpen = true;

    this.trigger();
  },

  onModalClose() {
    ActorClient.unbindUser(ActorClient.getUid(), this.setProfile);
    _isModalOpen = false;

    this.trigger();
  }
});
