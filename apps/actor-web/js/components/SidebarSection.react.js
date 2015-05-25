var React = require('react');
var HeaderSection = require('./sidebar/HeaderSection.react');
var RecentSection = require('./sidebar/RecentSection.react');

var SidebarSection = React.createClass({
  render: function() {
    return (
      <aside className="sidebar">
        <HeaderSection/>
        <RecentSection/>
      </aside>
    )
  }
});

module.exports = SidebarSection;
