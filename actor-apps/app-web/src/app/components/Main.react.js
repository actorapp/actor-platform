import React from 'react';

import requireAuth from 'utils/require-auth';
import ActorClient from 'utils/ActorClient';
import PeerUtils from 'utils/PeerUtils';

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

    document.addEventListener(visibilitychange, onVisibilityChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }

    const peer = PeerUtils.stringToPeer(this.props.params.id);

    if (peer) {
      DialogActionCreators.selectDialogPeer(peer);
    }
  }

  render() {
    const peer = PeerUtils.stringToPeer(this.props.params.id);

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
