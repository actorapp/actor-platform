import React from 'react';

import HeaderSection from './sidebar/HeaderSection.react';
import RecentSection from './sidebar/RecentSection.react';

import ContactActionCreators from '../actions/ContactActionCreators';

export default React.createClass({
  render: function() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

        <RecentSection/>

        <footer>
          <a onClick={this._onClick} className="button button--primary button--wide">
            <i className="material-icons">group</i> Contacts
          </a>
        </footer>
      </aside>
    );
  },

  _onClick: function() {
    ContactActionCreators.showContactList();
  }
});
