var React = require('react');
var HeaderSection = require('./sidebar/HeaderSection.react');
var RecentSection = require('./sidebar/RecentSection.react');

var SidebarSection = React.createClass({
  propTypes: {
    messenger: React.PropTypes.object.isRequired
  },

  render: function() {
    var messenger = this.props.messenger;

    return (
      <aside className="sidebar">
        <HeaderSection messenger={messenger}/>
        <RecentSection messenger={messenger}/>
      </aside>
    )
  }
});

module.exports = SidebarSection;
