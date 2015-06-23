import React from 'react';

import MyProfileActions from '../../actions/MyProfileActions';
import CreateGroupActionCreators from '../../actions/CreateGroupActionCreators';

import AvatarItem from '../common/AvatarItem.react';
import MyProfileModal from '../modals/MyProfile.react';
import ActorClient from '../../utils/ActorClient';

import classNames from 'classnames';

var getStateFromStores = () => {
  return {dialogInfo: null};
};

class HeaderSection extends React.Component {
  componentWillMount() {
    ActorClient.bindUser(ActorClient.getUid(), this._setUser);
  }

  constructor() {
    super();

    this._setUser = this._setUser.bind(this);
    this._toggleHeaderMenu = this._toggleHeaderMenu.bind(this);
    this.openCreateGroup = this.openCreateGroup.bind(this);
    this.openMyProfile = this.openMyProfile.bind(this);
    this._setLogout = this._setLogout.bind(this);

    this.state = getStateFromStores();
  }

  _setUser(user) {
    this.setState({user: user});
  }

  _toggleHeaderMenu() {
    this.setState({isOpened: !this.state.isOpened});
  }

  _setLogout() {
    localStorage.clear();
    location.reload();
  }

  render() {
    var user = this.state.user;

    if (user) {

      var headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': this.state.isOpened
      });

      return (
        <header className={headerClass}>
          <div className="sidebar__header__user row" onClick={this._toggleHeaderMenu}>
            <AvatarItem image={user.avatar}
                        placeholder={user.placeholder}
                        size="small"
                        title={user.name} />
            <span className="sidebar__header__user__name col-xs">{user.name}</span>
            <span className="sidebar__header__user__expand">
              <i className="material-icons">keyboard_arrow_down</i>
            </span>
          </div>
          <ul className="sidebar__header__menu">
            <li className="sidebar__header__menu__item" onClick={this.openMyProfile}>
              <span>Profile</span>
            </li>
            <li className="sidebar__header__menu__item" onClick={this.openCreateGroup}>
              <span>Create group</span>
            </li>
            <li className="sidebar__header__menu__item hide"><span>Integrations</span></li>
            <li className="sidebar__header__menu__item hide"><span>Settings</span></li>
            <li className="sidebar__header__menu__item hide"><span>Help</span></li>
            <li className="sidebar__header__menu__item" onClick={this._setLogout}>
              <span>Log out</span>
            </li>
          </ul>

          <MyProfileModal/>
        </header>
      );
    } else {
      return null;
    }
  }

  openMyProfile() {
    MyProfileActions.modalOpen();
    this.setState({isOpened: false});
  }

  openCreateGroup() {
    CreateGroupActionCreators.openModal();
  }
}

export default HeaderSection;
