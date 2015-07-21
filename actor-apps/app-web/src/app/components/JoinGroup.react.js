import React from 'react';

import requireAuth from 'utils/require-auth';

import DialogActionCreators from 'actions/DialogActionCreators';

import JoinGroupActions from 'actions/JoinGroupActions';
import JoinGroupStore from 'stores/JoinGroupStore'; // eslint-disable-line

class JoinGroup extends React.Component {
  static propTypes = {
    params: React.PropTypes.object
  };

  static contextTypes = {
    router: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    JoinGroupActions.joinGroup(props.params.token)
      .then((peer) => {
        this.context.router.replaceWith('/');
        DialogActionCreators.selectDialogPeer(peer);
      }).catch((e) => {
        console.warn(e, 'User is already a group member');
        this.context.router.replaceWith('/');
      });
  }

  render() {
    return null;
  }
}

export default requireAuth(JoinGroup);
