/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';

import { PeerTypes, KeyCodes } from 'constants/ActorAppConstants';

import requireAuth from 'utils/require-auth';
import ActorClient from 'utils/ActorClient';
import PeerUtils from 'utils/PeerUtils';
import RouterContainer from 'utils/RouterContainer';
import { preloadEmojiSheet } from 'utils/EmojiUtils'

import DialogActionCreators from 'actions/DialogActionCreators';
import VisibilityActionCreators from 'actions/VisibilityActionCreators';
import FastSwitcherActionCreators from 'actions/FastSwitcherActionCreators';

import SidebarSection from 'components/SidebarSection.react';
import DialogSection from 'components/DialogSection.react';
import Favicon from 'components/common/Favicon.react';

class Main extends Component {
  static contextTypes = {
    router: React.PropTypes.func
  };

  static propTypes = {
    params: React.PropTypes.object
  };

  constructor(props) {
    super(props);

    const { params } = props;
    const peer = PeerUtils.stringToPeer(params.id);

    document.addEventListener('visibilitychange', this.onVisibilityChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    // Preload emoji spritesheet
    preloadEmojiSheet();

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }

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

  onVisibilityChange = () => {
    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    } else {
      VisibilityActionCreators.createAppHidden();
    }
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.K && event.metaKey) {
      FastSwitcherActionCreators.show();
    }
  };

  render() {
    const { params } = this.props;
    const peer = PeerUtils.stringToPeer(params.id);

    return (
      <div className="app">
        <Favicon/>
        <SidebarSection selectedPeer={peer}/>
        <DialogSection peer={peer}/>
      </div>
    );
  }
}

export default requireAuth(Main);
