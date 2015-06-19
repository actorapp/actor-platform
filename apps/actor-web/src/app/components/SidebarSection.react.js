import React from 'react';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';

import ContactActionCreators from '../actions/ContactActionCreators';

class SidebarSection extends React.Component {
  constructor() {
    super();

    this.onContactsClick = this.onContactsClick.bind(this);
  }

  render() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

        <RecentSection/>

        <footer>
          <a className="button button--blue button--wide" onClick={this.onContactsClick}>
            <i className="material-icons">group</i> Contacts
          </a>
        </footer>
      </aside>
    );
  }

  onContactsClick() {
    ContactActionCreators.showContactList();
  }
}

export default SidebarSection;
