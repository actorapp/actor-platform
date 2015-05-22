var React = require('react');

var AvatarItem = require('../common/AvatarItem.react');

var HeaderSection = React.createClass({
  propTypes: {
    messenger: React.PropTypes.object.isRequired
  },

  getInitialState: function() {
    return {isOpened: false};
  },

  componentWillMount: function() {
    var messenger = this.props.messenger;
    messenger.bindUser(messenger.getUid(), this._setUser)
  },

  render: function() {
    var user = this.state.user;

    var headerClass = 'sidebar__header sidebar__header--clickable';

    if (this.state.isOpened) {
      headerClass += ' sidebar__header--opened';
    }

    return (
      <header className={headerClass}>
        <div className="sidebar__header__user row" onClick={this._toggleHeaderMenu}>
          <div className="sidebar__header__user__avatar avatar avatar--small">
            <AvatarItem title={user.name} image={user.avatar} placeholder={user.placeholder}/>
          </div>
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
