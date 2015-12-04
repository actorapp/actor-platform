/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

import HeaderSection from './sidebar/HeaderSection.react';
import DefaultRecentSection from './sidebar/RecentSection.react';

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

    const RecentSection = delegate.components.recent || DefaultRecentSection;

    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection selectedPeer={selectedPeer}/>
      </aside>
    );
  }
}

export default SidebarSection;
