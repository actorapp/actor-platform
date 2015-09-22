/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';

import HeaderSection from 'components/sidebar/HeaderSection.react';
import RecentSection from 'components/sidebar/RecentSection.react';

class SidebarSection extends React.Component {
  static propTypes = {
    selectedPeer: React.PropTypes.object
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { selectedPeer } = this.props;

    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection selectedPeer={selectedPeer}/>
      </aside>
    );
  }
}

export default SidebarSection;
