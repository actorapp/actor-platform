/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, {Component, PropTypes} from 'react';

import HeaderSection from 'components/sidebar/HeaderSection.react';
import RecentSection from 'components/sidebar/RecentSection.react';

export default class SidebarSection extends Component {
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
