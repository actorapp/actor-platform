var React = require('react');
var UserSection = require('./sidebar/UserSection.react');
var RecentSection = require('./sidebar/RecentSection.react');

var SidebarSection = React.createClass({
  propTypes: {
    messenger: React.PropTypes.object.isRequired
  },

  render: function() {
    var messenger = this.props.messenger;

    return(
      <aside className="sidebar">
        <header className="sidebar__header sidebar__header--clickable">
          <UserSection messenger={messenger}/>
          <ul className="sidebar__header__menu">
            <li className="sidebar__header__menu__item"><span>Profile</span></li>
            <li className="sidebar__header__menu__item"><span>Integrations</span></li>
            <li className="sidebar__header__menu__item"><span>Settings</span></li>
            <li className="sidebar__header__menu__item"><span>Help</span></li>
            <li className="sidebar__header__menu__item"><span>Log out</span></li>
          </ul>
        </header>
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
