var React = require('react');

var HeaderSection = require('./sidebar/HeaderSection.react');
var RecentSection = require('./sidebar/RecentSection.react');

var ContactActionCreators = require('../actions/ContactActionCreators');


var SidebarSection = React.createClass({
  render: function() {
    return (
      <aside className="sidebar">
        <HeaderSection/>

        <RecentSection/>

        <footer className="hide">
          <a onClick={this._onClick} className="button button--primary button--wide">
            <i className="material-icons">group</i> Contacts
          </a>
        </footer>
      </aside>
    )
  },

  _onClick: function() {
    ContactActionCreators.showContactList();
  }
});

module.exports = SidebarSection;
