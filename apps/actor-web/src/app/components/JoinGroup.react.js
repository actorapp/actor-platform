import React from 'react';

import requireAuth from '../utils/require-auth';

import DialogActionCreators from '../actions/DialogActionCreators';

import JoinGroupActions from '../actions/JoinGroupActions';
import JoinGroupStore from '../stores/JoinGroupStore'; // eslint-disable-line

class JoinGroup extends React.Component {
  componentWillMount() {
    JoinGroupActions.joinGroup(this.props.params.token)
      .then((peer) => {
        this.context.router.replaceWith('/');
        DialogActionCreators.selectDialogPeer(peer);
      }).catch((e) => {
        console.warn(e, 'User is already a group member');
        this.context.router.replaceWith('/');
      });
  }

  constructor() {
    super();
  }

  render() {
    return null;
  }
}

JoinGroup.contextTypes = {
  router: React.PropTypes.func
};

export default requireAuth(JoinGroup);
