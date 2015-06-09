'use strict';

import Reflux from 'reflux';

import InviteUserActions from '../actions/InviteUserActions';

let
  _isModalOpen = false,
  _group = null;

let InviteUserStore = Reflux.createStore({
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

  onModalOpen (group) {
    _isModalOpen = true;
    _group = group;

    this.trigger();
  },

  onModalClose () {
    _isModalOpen = false;
    _group = null;

    this.trigger();
  }
});

module.exports = InviteUserStore;
