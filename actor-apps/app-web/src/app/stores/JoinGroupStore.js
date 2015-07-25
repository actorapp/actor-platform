import Reflux from 'reflux';

import ActorClient from 'utils/ActorClient';

import JoinGroupActions from 'actions/JoinGroupActions';

const urlBase = 'https://quit.email';

export default Reflux.createStore({
  init () {
    this.listenTo(JoinGroupActions.joinGroup, this.onJoin);
  },

  onJoin (token) {
    let url = urlBase + '/join/' + token;

    return JoinGroupActions.joinGroup.promise(ActorClient.joinGroup(url));
  },

  getUrlBase () {
    return urlBase;
  }
});
