import React from 'react';

import requireAuth from 'utils/require-auth';
import ActorClient from 'utils/ActorClient';
import PeerUtils from 'utils/PeerUtils';

import DialogActionCreators from 'actions/DialogActionCreators';
import VisibilityActionCreators from 'actions/VisibilityActionCreators';
import FaviconActionCreators from 'actions/FaviconActionCreators';
import FaviconStore from 'stores/FaviconStore';

import SidebarSection from 'components/SidebarSection.react';
import DialogSection from 'components/DialogSection.react';
import Favicon from 'components/common/Favicon.react';
import Banner from 'components/common/Banner.react';

const visibilitychange = 'visibilitychange';

const onVisibilityChange = () => {
  if (!document.hidden) {
    VisibilityActionCreators.createAppVisible();
    FaviconActionCreators.setDefaultFavicon();
  } else {
    VisibilityActionCreators.createAppHidden();
  }
};

const getStateFromStores = () => {
  return {
    faviconPath: FaviconStore.getFaviconPath()
  };
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

    this.state = getStateFromStores();

    document.addEventListener(visibilitychange, onVisibilityChange);
    FaviconStore.addChangeListener(this.onChange);

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }

    const peer = PeerUtils.stringToPeer(this.props.params.id);

    if (peer) {
      DialogActionCreators.selectDialogPeer(peer);
    }
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  render() {
    const peer = PeerUtils.stringToPeer(this.props.params.id);

    return (
      <div className="app">
        <Favicon path={this.state.faviconPath}/>
        <Banner/>
        <SidebarSection selectedPeer={peer}/>
        <DialogSection peer={peer}/>

      </div>
    );
  }
}

export default requireAuth(Main);
