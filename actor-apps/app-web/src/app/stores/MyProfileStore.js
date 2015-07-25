import Reflux from 'reflux';

import ActorClient from 'utils/ActorClient';

import MyProfileActions from 'actions/MyProfileActions';

let
  _profile = null,
  _name = null,
  _isModalOpen = false;

export default Reflux.createStore({
  init() {
    this.listenTo(MyProfileActions.modalOpen, this.onModalOpen);
    this.listenTo(MyProfileActions.modalClose, this.onModalClose);
    this.listenTo(MyProfileActions.setName, this.setName);
  },

  isModalOpen() {
    return _isModalOpen;
  },

  getProfile() {
    return _profile;
  },

  getName() {
    return _name;
  },

  setName(name) {
    ActorClient.editMyName(name);
  },

  setProfile(profile) {
    _profile = profile;
    _name = profile.name;

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
