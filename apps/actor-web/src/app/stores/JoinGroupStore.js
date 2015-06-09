'use strict';

import Reflux from 'reflux'

import ActorClient from '../utils/ActorClient'

import JoinGroupActions from '../actions/JoinGroupActions'

let JoinGroupStore = Reflux.createStore({
  init () {
    this.listenTo(JoinGroupActions.joinGroup, this.onJoin)
  },

  onJoin (token) {
    ActorClient.joinGroup(token);
  }
});

module.exports = JoinGroupStore;
