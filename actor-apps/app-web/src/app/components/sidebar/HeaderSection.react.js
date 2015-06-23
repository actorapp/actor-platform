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
    ActorClient.bindUser(ActorClient.getUid(), this.setUser);
  }

  constructor() {
    super();

    this.setUser = this.setUser.bind(this);
    this.toggleHeaderMenu = this.toggleHeaderMenu.bind(this);
    this.openCreateGroup = this.openCreateGroup.bind(this);
    this.openMyProfile = this.openMyProfile.bind(this);
    this.setLogout = this.setLogout.bind(this);

    this.state = getStateFromStores();
  }

  setUser(user) {
    this.setState({user: user});
  }

  toggleHeaderMenu() {
    this.setState({isOpened: !this.state.isOpened});
  }

  setLogout() {
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
          <div className="sidebar__header__user row" onClick={this.toggleHeaderMenu}>
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
              <i className="material-icons">person</i>
              <span>Profile</span>
            </li>
            <li className="sidebar__header__menu__item" onClick={this.openCreateGroup}>
              <i className="material-icons">group_add</i>
              <span>Create group</span>
            </li>
            <li className="sidebar__header__menu__item hide">
              <i className="material-icons">cached</i>
              <span>Integrations</span>
            </li>
            <li className="sidebar__header__menu__item hide">
              <i className="material-icons">settings</i>
              <span>Settings</span>
            </li>
            <li className="sidebar__header__menu__item hide">
              <i className="material-icons">help</i>
              <span>Help</span>
            </li>
            <li className="sidebar__header__menu__item" onClick={this.setLogout}>
              <i className="material-icons">power_settings_new</i>
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
