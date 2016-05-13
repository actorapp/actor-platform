/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */
import { isFunction } from 'lodash';
import React, { Component, PropTypes } from 'react';
import DelegateContainer from '../utils/DelegateContainer';

import VisibilityActionCreators from '../actions/VisibilityActionCreators';

import DefaultSidebar from './Sidebar.react';
import DefaultToolbar from './Toolbar.react';
import ConnectionState from './common/ConnectionState.react';
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

  constructor(props) {
    super(props);

    this.components = this.getComponents();
  }

  getComponents() {
    const { components } = DelegateContainer.get();

    if (components) {
      return {
        Sidebar: isFunction(components.sidebar) ? components.sidebar : DefaultSidebar,
        Toolbar: isFunction(components.toolbar) ? components.toolbar : DefaultToolbar
      };
    }

    return {
      Sidebar: DefaultSidebar,
      Toolbar: DefaultToolbar
    };
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
    const { features } = DelegateContainer.get();

    if (!features.calls) {
      return null;
    }

    return <SmallCall />;
  }

  render() {
    const { Sidebar, Toolbar } = this.components;

    return (
      <div className="app">
        <ConnectionState/>
        <Favicon/>

        <Toolbar/>
        <section className="wrapper">
          <Sidebar/>
          {this.props.children}
        </section>


        <ModalsWrapper/>
        <MenuOverlay/>

        {this.renderCall()}
      </div>
    );
  }
}

export default Main;
