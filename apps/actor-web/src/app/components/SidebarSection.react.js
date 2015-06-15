import React from 'react';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';

import ContactActionCreators from '../actions/ContactActionCreators';

class SidebarSection extends React.Component {
  constructor() {
    super();

    this.onClick = this.onClick.bind(this);
  }

  onClick() {
    ContactActionCreators.showContactList();
  }

  render() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

        <RecentSection/>

        <footer>
          <a className="button button--primary button--wide" onClick={this.onClick}>
            <i className="material-icons">group</i> Contacts
          </a>
        </footer>
      </aside>
    );
  }
}

export default SidebarSection;
