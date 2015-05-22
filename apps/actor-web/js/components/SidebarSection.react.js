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
        <ul className="sidebar__list">
          <li className="sidebar__list__title">
            Starred
          </li>
          <li className="sidebar__list__item">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--yellow">D</span>
            </div>
        <span>
          Starred
        </span>
          </li>
          <li className="sidebar__list__item sidebar__list__item--active">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--purple">H</span>
            </div>
        <span>
          Starred conversation 2
        </span>
          </li>
          <li className="sidebar__list__item">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--green">G</span>
            </div>
        <span>
          Starred conversation 3
        </span>
          </li>
        </ul>
        <RecentSection messenger={messenger}/>
      </aside>
    )
  }
});

module.exports = SidebarSection;
