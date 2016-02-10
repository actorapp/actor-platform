/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import { PeerTypes, KeyCodes } from '../constants/ActorAppConstants';

import requireAuth from '../utils/require-auth';
import ActorClient from '../utils/ActorClient';
import PeerUtils from '../utils/PeerUtils';
import RouterContainer from '../utils/RouterContainer';
import { preloadEmojiSheet } from '../utils/EmojiUtils'

import DialogActionCreators from '../actions/DialogActionCreators';
import VisibilityActionCreators from '../actions/VisibilityActionCreators';
import QuickSearchActionCreators from '../actions/QuickSearchActionCreators';

import DefaultSidebarSection from './SidebarSection.react';
import DefaultDialogSection from './DialogSection.react';
import Favicon from './common/Favicon.react';

import ModalsWrapper from './modals/ModalsWrapper.react';
import CallModal from './modals/CallModal.react';
import DropdownWrapper from './common/DropdownWrapper.react';

class Main extends Component {
  static contextTypes = {
    router: PropTypes.func,
    delegate: PropTypes.object
  };

  static propTypes = {
    params: PropTypes.object
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
    // TODO: Make this hotkey work on windows
    if (event.keyCode === KeyCodes.K && event.metaKey) {
      event.stopPropagation();
      event.preventDefault();
      QuickSearchActionCreators.show();
    }
  };

  render() {
    const { params } = this.props;
    const { delegate } = this.context;
    const peer = PeerUtils.stringToPeer(params.id);

    const SidebarSection = (typeof delegate.components.sidebar == 'function') ? delegate.components.sidebar : DefaultSidebarSection;
    const DialogSection = (typeof delegate.components.dialog == 'function') ? delegate.components.dialog : DefaultDialogSection;

    return (
      <div className="app">
        <Favicon/>

        <SidebarSection selectedPeer={peer}/>
        <DialogSection peer={peer}/>

        <ModalsWrapper/>
        <DropdownWrapper/>
        <CallModal/>
      </div>
    );
  }
}

export default requireAuth(Main);
