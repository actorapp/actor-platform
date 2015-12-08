/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import DefaultHeaderSection from './sidebar/HeaderSection.react';
import DefaultRecentSection from './sidebar/RecentSection.react';
import QuickSearchButton from './sidebar/QuickSearchButton.react';

class SidebarSection extends Component {
  constructor(props){
    super(props);
  }

  static contextTypes = {
    delegate: PropTypes.object
  };

  static propTypes = {
    selectedPeer: PropTypes.object.isRequired
  };

  render() {
    const { selectedPeer } = this.props;
    const { delegate } = this.context;

    let HeaderSection, RecentSection, FooterSection;
    if (delegate.components.sidebar !== null && typeof delegate.components.sidebar !== 'function') {
      HeaderSection = delegate.components.sidebar.header || DefaultHeaderSection;
      RecentSection = delegate.components.sidebar.recent || DefaultRecentSection;
      FooterSection = delegate.components.sidebar.footer || QuickSearchButton;
    } else {
      HeaderSection = DefaultHeaderSection;
      RecentSection = DefaultRecentSection;
      FooterSection = QuickSearchButton;
    }

    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection selectedPeer={selectedPeer}/>
        <FooterSection/>
      </aside>
    );
  }
}

export default SidebarSection;
