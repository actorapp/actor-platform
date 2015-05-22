var React = require('react');

var SidebarSection = React.createClass({
  render: function() {
    return(
      <aside className="sidebar">
        <header className="sidebar__header sidebar__header--clickable">
          <div className="sidebar__header__user row">
            <div className="sidebar__header__user__avatar avatar avatar--small">
              <span className="avatar__placeholder avatar__placeholder--yellow">O</span>
            </div>
            <span className="sidebar__header__user__name">Oleg Shilov</span>
            <span className="col-xs"></span>
            <img className="sidebar__header__user__expand" src="assets/img/icons/png/ic_expand_more_2x_white.png" alt=""/>
          </div>
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
        <ul className="sidebar__list">
          <li className="sidebar__list__title">
            Recent
          </li>
          <li className="sidebar__list__item">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--blue">R</span>
            </div>
        <span>
          Recent conversation 1
        </span>
          </li>
          <li className="sidebar__list__item">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--lblue">С</span>
            </div>
        <span>
          Recent conversation 2
        </span>
          </li>
          <li className="sidebar__list__item sidebar__list__item--active2">
            <div className="avatar avatar--tiny">
              <span className="avatar__placeholder avatar__placeholder--red">W</span>
            </div>
        <span>
          Recent 3
        </span>
          </li>
        </ul>
      </aside>
    )
  }
});

module.exports = SidebarSection;
