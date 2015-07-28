import React from 'react';
import mixpanel from 'utils/Mixpanel';

import MyProfileActions from 'actions/MyProfileActions';
import LoginActionCreators from 'actions/LoginActionCreators';
import HelpActionCreators from 'actions/HelpActionCreators';

import AvatarItem from 'components/common/AvatarItem.react';
import MyProfileModal from 'components/modals/MyProfile.react';
import ActorClient from 'utils/ActorClient';

import classNames from 'classnames';

var getStateFromStores = () => {
  return {dialogInfo: null};
};

class HeaderSection extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();
  }

  componentDidMount() {
    ActorClient.bindUser(ActorClient.getUid(), this.setUser);
  }

  setUser = (user) => {
    this.setState({user: user});
  };

  toggleHeaderMenu = () => {
    mixpanel.track('Open sidebar menu');
    this.setState({isOpened: !this.state.isOpened});
  };

  setLogout = () => {
    LoginActionCreators.setLoggedOut();
  };

  openMyProfile = () => {
    MyProfileActions.modalOpen();
    mixpanel.track('My profile open');
    this.setState({isOpened: false});
  };

  openHelpDialog = () => {
    HelpActionCreators.open();
  };

  render() {
    const user = this.state.user;

    if (user) {

      let headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
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
            {/*
            <li className="sidebar__header__menu__item" onClick={this.openCreateGroup}>
              <i className="material-icons">group_add</i>
              <span>Create group</span>
            </li>
             */}
            <li className="sidebar__header__menu__item hide">
              <i className="material-icons">cached</i>
              <span>Integrations</span>
            </li>
            <li className="sidebar__header__menu__item hide">
              <i className="material-icons">settings</i>
              <span>Settings</span>
            </li>
            <li className="sidebar__header__menu__item" onClick={this.openHelpDialog}>
              <i className="material-icons">help</i>
              <span>Help and feedback</span>
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
}

export default HeaderSection;
