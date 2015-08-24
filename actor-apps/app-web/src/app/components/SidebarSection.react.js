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
    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection selectedPeer={this.props.selectedPeer}/>
      </aside>
    );
  }
}

export default SidebarSection;
