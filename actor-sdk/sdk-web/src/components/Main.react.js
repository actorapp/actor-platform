/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { preloadEmojiSheet } from '../utils/EmojiUtils'

import VisibilityActionCreators from '../actions/VisibilityActionCreators';

import DefaultSidebar from './Sidebar.react';
import Favicon from './common/Favicon.react';

import ModalsWrapper from './modals/ModalsWrapper.react';
import MenuOverlay from './common/MenuOverlay.react';
import SmallCall from './SmallCall.react';

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
    // TODO: Fix! Its not working properly.
    preloadEmojiSheet();
  }

  componentDidMount() {
    this.onVisibilityChange();
    document.addEventListener('visibilitychange', this.onVisibilityChange);
  }

  componentWillUnmount() {
    document.removeEventListener('visibilitychange', this.onVisibilityChange);
  }

  onVisibilityChange = () => {
    if (document.hidden) {
      VisibilityActionCreators.createAppHidden();
    } else {
      VisibilityActionCreators.createAppVisible();
    }
  };

  renderCall() {
    const { delegate } = this.context;
    if (!delegate.features.calls) {
      return null;
    }

    return <SmallCall />;
  }

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

        {this.renderCall()}
      </div>
    );
  }
}

export default Main;
