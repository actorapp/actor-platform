/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, {Component, PropTypes} from 'react';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';

class SidebarSection extends Component {
  constructor(props){
    super(props);
  }

  static propTypes = {
    selectedPeer: PropTypes.object.isRequired
  };

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
