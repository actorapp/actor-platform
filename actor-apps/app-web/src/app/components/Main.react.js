import React from 'react';

import {PeerTypes} from 'constants/ActorAppConstants';

import requireAuth from 'utils/require-auth';
import ActorClient from 'utils/ActorClient';
import PeerUtils from 'utils/PeerUtils';
import RouterContainer from 'utils/RouterContainer';

import DialogActionCreators from 'actions/DialogActionCreators';
import VisibilityActionCreators from 'actions/VisibilityActionCreators';

import SidebarSection from 'components/SidebarSection.react';
import DialogSection from 'components/DialogSection.react';
import Banner from 'components/common/Banner.react';
import Favicon from 'components/common/Favicon.react';

const visibilitychange = 'visibilitychange';

const onVisibilityChange = () => {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible();
  } else {
    VisibilityActionCreators.createAppHidden();
  }
};

class Main extends React.Component {
  static contextTypes = {
    router: React.PropTypes.func
  };

  static propTypes = {
    params: React.PropTypes.object
  };

  constructor(props) {
    super(props);

    const { params } = props;

    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }

    const peer = PeerUtils.stringToPeer(params.id);

    if (peer) {
      // It is needed to prevent failure on opening dialog while library didn't load dialogs (right after auth)
      let peerInfo = undefined;

      if (peer.type == PeerTypes.GROUP) {
        peerInfo = ActorClient.getGroup(peer.id)
      } else {
        peerInfo = ActorClient.getUser(peer.id)
      }

      if (peerInfo) {
        DialogActionCreators.selectDialogPeer(peer);
      } else {
        RouterContainer.get().transitionTo('/');
      }
    }
  }

  render() {
    const { params } = this.props;
    const peer = PeerUtils.stringToPeer(params.id);

    return (
      <div className="app">
        <Favicon/>
        <Banner/>
        <SidebarSection selectedPeer={peer}/>
        <DialogSection peer={peer}/>
      </div>
    );
  }
}

export default requireAuth(Main);
