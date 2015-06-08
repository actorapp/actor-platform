'use strict';

var React = require('react');

var AvatarItem = require('../common/AvatarItem.react');

var ModalActionCreators = require('../../actions/ModalActionCreators');

var classNames = require('classnames');


var HeaderSection = React.createClass({
  getInitialState: function() {
    return {isOpened: false};
  },

  componentDidMount: function() {
    window.messenger.bindUser(window.messenger.getUid(), this._setUser)
  },

  render: function() {
    var user = this.state.user;

    if (user) {

      var headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': this.state.isOpened
      });

      return (
        <header className={headerClass}>
          <div className="sidebar__header__user row" onClick={this._toggleHeaderMenu}>
            <AvatarItem title={user.name} image={user.avatar} placeholder={user.placeholder} size="small"/>
            <span className="sidebar__header__user__name">{user.name}</span>
            <span className="col-xs"></span>
            <span className="sidebar__header__user__expand">
              <i className="material-icons">keyboard_arrow_down</i>
            </span>
          </div>
          <ul className="sidebar__header__menu">
            <li className="sidebar__header__menu__item" onClick={this._openMyProfile}>
              <span>Profile</span>
            </li>
            <li className="sidebar__header__menu__item hide"><span>Integrations</span></li>
            <li className="sidebar__header__menu__item hide"><span>Settings</span></li>
            <li className="sidebar__header__menu__item hide"><span>Help</span></li>
            <li className="sidebar__header__menu__item" onClick={this._setLogout}>
              <span>Log out</span>
            </li>
          </ul>
        </header>
      );
    } else {
      return null;
    }
  },

  _setUser: function(user) {
    this.setState({user: user});
  },

  _toggleHeaderMenu: function() {
    this.setState({isOpened: !this.state.isOpened});
  },

  _setLogout: function() {
    localStorage.clear();
    location.reload();
  },

  _openMyProfile: function() {
    this.setState({isOpened: false});
    ModalActionCreators.show({title: 'Profile view', content: 'Profile body content'});
  }

});

module.exports = HeaderSection;
