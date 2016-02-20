/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { PeerTypes, KeyCodes } from '../constants/ActorAppConstants';

import PeerUtils from '../utils/PeerUtils';
import history from '../utils/history';
import { preloadEmojiSheet } from '../utils/EmojiUtils'

import DialogActionCreators from '../actions/DialogActionCreators';
import VisibilityActionCreators from '../actions/VisibilityActionCreators';
import QuickSearchActionCreators from '../actions/QuickSearchActionCreators';

import UserStore from '../stores/UserStore';
import GroupStore from '../stores/GroupStore';

import DefaultSidebar from './Sidebar.react';
import DefaultDialog from './Dialog.react';
import Favicon from './common/Favicon.react';

import ModalsWrapper from './modals/ModalsWrapper.react';
import DropdownWrapper from './common/DropdownWrapper.react';
import CallModal from './modals/CallModal.react';
import InviteUser from './modals/InviteUser.react';
import InviteByLink from './modals/invite-user/InviteByLink.react';
import EditGroup from './modals/EditGroup.react';

class Main extends Component {
  static propTypes = {
    params: PropTypes.object
  };

  static contextTypes = {
    delegate: PropTypes.object
  };

  constructor(props) {
    super(props);

    document.addEventListener('visibilitychange', this.onVisibilityChange);
    document.addEventListener('keydown', this.onKeyDown, false);

    // Preload emoji spritesheet
    preloadEmojiSheet();

    if (!document.hidden) {
      VisibilityActionCreators.createAppVisible();
    }
  }

  componentDidMount() {
    const { params } = this.props;
    const peer = PeerUtils.stringToPeer(params.id);

    if (peer) {
      // It is needed to prevent failure on opening dialog while library didn't load dialogs (right after auth)
      let peerInfo = undefined;

      if (peer.type == PeerTypes.GROUP) {
        peerInfo = GroupStore.getGroup(peer.id)
      } else {
        peerInfo = UserStore.getUser(peer.id)
      }

      if (peerInfo) {
        DialogActionCreators.selectDialogPeer(peer);
      } else {
        history.replace('/');
      }
    }
  }

  componentWillReceiveProps(nextProps) {
    const { params } = nextProps;
    if (this.props.params.id !== params.id) {
      const peer = PeerUtils.stringToPeer(params.id);
      if (peer) {
        DialogActionCreators.selectDialogPeer(peer);
      } else {
        history.push('/');
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

    const Sidebar = (typeof delegate.components.sidebar == 'function') ? delegate.components.sidebar : DefaultSidebar;
    const Dialog = (typeof delegate.components.dialog == 'function') ? delegate.components.dialog : DefaultDialog;

    return (
      <div className="app">
        <Favicon/>

        <Sidebar/>
        <Dialog/>

        <ModalsWrapper/>
        <DropdownWrapper/>
        <InviteUser/>
        <InviteByLink/>
        <EditGroup/>
        <CallModal/>
      </div>
    );
  }
}

export default Main;
