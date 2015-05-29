var AvatarItem = require('../common/AvatarItem.react');
var classNames = require('classnames');
var React = require('react');

var HeaderSection = React.createClass({
  getInitialState: function() {
    return {isOpened: false};
  },

  componentWillMount: function() {
    window.messenger.bindUser(window.messenger.getUid(), this._setUser)
  },

  render: function() {
    var user = this.state.user;

    var headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
      'sidebar__header--opened': this.state.isOpened
    });

    return (
      <header className={headerClass}>
        <div className="sidebar__header__user row" onClick={this._toggleHeaderMenu}>
          <AvatarItem title={user.name} image={user.avatar} placeholder={user.placeholder} size="small"/>
          <span className="sidebar__header__user__name">{user.name}</span>
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
    );
  },

  _setUser: function(user) {
    this.setState({user: user});
  },

  _toggleHeaderMenu: function() {
    this.setState({isOpened: !this.state.isOpened});
  }

});

module.exports = HeaderSection;
