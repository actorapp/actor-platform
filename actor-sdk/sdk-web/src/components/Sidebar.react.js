/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import DefaultHeaderSection from './sidebar/HeaderSection.react';
import DefaultRecent from './sidebar/Recent.react';
import QuickSearchButton from './sidebar/QuickSearchButton.react';

import DialogStore from '../stores/DialogStore';

class SidebarSection extends Component {
  constructor(props){
    super(props);
  }

  static getStores = () => [DialogStore];

  static calculateState() {
    return {
      dialogs: DialogStore.getDialogs()
    }
  };

  static contextTypes = {
    delegate: PropTypes.object
  };

  render() {
    const { delegate } = this.context;
    const { dialogs } = this.state;

    let HeaderSection, Recent, FooterSection;
    if (delegate.components.sidebar !== null && typeof delegate.components.sidebar !== 'function') {
      HeaderSection = delegate.components.sidebar.header || DefaultHeaderSection;
      Recent = delegate.components.sidebar.recent || DefaultRecent;
      FooterSection = delegate.components.sidebar.footer || QuickSearchButton;
    } else {
      HeaderSection = DefaultHeaderSection;
      Recent = DefaultRecent;
      FooterSection = QuickSearchButton;
    }

    return (
      <aside className="sidebar">
        <HeaderSection/>
        <Recent dialogs={dialogs}/>
        <FooterSection/>
      </aside>
    );
  }
}

export default Container.create(SidebarSection, {pure: false});
