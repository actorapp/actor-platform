/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import DefaultHeaderSection from './sidebar/HeaderSection.react';
import DefaultRecent from './sidebar/Recent.react';
import QuickSearchButton from './sidebar/QuickSearchButton.react';

class SidebarSection extends Component {
  constructor(props){
    super(props);
  }

  static contextTypes = {
    delegate: PropTypes.object
  };

  render() {
    const { delegate } = this.context;

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
        <Recent/>
        <FooterSection/>
      </aside>
    );
  }
}

export default SidebarSection;
