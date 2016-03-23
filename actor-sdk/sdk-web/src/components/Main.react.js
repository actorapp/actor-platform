/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { KeyCodes } from '../constants/ActorAppConstants';

import { preloadEmojiSheet } from '../utils/EmojiUtils'

import VisibilityActionCreators from '../actions/VisibilityActionCreators';
import QuickSearchActionCreators from '../actions/QuickSearchActionCreators';

import DefaultSidebar from './Sidebar.react';
import Favicon from './common/Favicon.react';

import ModalsWrapper from './modals/ModalsWrapper.react';
import MenuOverlay from './common/MenuOverlay.react';
import InviteUser from './modals/InviteUser.react';
import InviteByLink from './modals/invite-user/InviteByLink.react';
import EditGroup from './modals/EditGroup.react';

class Main extends Component {
  static propTypes = {
    params: PropTypes.object,
    children: PropTypes.oneOfType([
      PropTypes.arrayOf(PropTypes.node),
      PropTypes.node
    ])
  };

  static contextTypes = {
    delegate: PropTypes.object
  };

  constructor(props) {
    super(props);

    // Preload emoji spritesheet
    preloadEmojiSheet();
  }

  componentDidMount() {
    this.onVisibilityChange();
    document.addEventListener('visibilitychange', this.onVisibilityChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentDidUnmount() {
    document.removeEventListener('visibilitychange', this.onVisibilityChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onVisibilityChange = () => {
    if (document.hidden) {
      VisibilityActionCreators.createAppHidden();
    } else {
      VisibilityActionCreators.createAppVisible();
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
    const { delegate } = this.context;

    const Sidebar = (typeof delegate.components.sidebar == 'function') ? delegate.components.sidebar : DefaultSidebar;

    return (
      <div className="app">
        <Favicon/>

        <Sidebar/>
        {this.props.children}

        <ModalsWrapper/>
        <MenuOverlay/>
        <InviteUser/>
        <InviteByLink/>
        <EditGroup/>
      </div>
    );
  }
}

export default Main;
