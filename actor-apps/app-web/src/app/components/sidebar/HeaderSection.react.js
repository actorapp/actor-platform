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
    this.setState({isOpened: false});
  };

  render() {
    const user = this.state.user;

    if (user) {

      let headerClass = classNames('sidebar__header', 'sidebar__header--clickable', {
        'sidebar__header--opened': this.state.isOpened
      });
      let menuClass = classNames('dropdown', {
        'dropdown--opened': this.state.isOpened
      });

      return (
        <header className={headerClass}>
          <div className="sidebar__header__user row" onClick={this.toggleHeaderMenu}>
            <AvatarItem image={user.avatar}
                        placeholder={user.placeholder}
                        size="tiny"
                        title={user.name} />
            <span className="sidebar__header__user__name col-xs">{user.name}</span>
            <div className={menuClass}>
              <span className="dropdown__button">
                <i className="material-icons">arrow_drop_down</i>
              </span>
              <ul className="dropdown__menu dropdown__menu--right">
                <li className="dropdown__menu__item">
                  <i className="material-icons">photo_camera</i>
                  Set Profile Photo
                </li>
                <li className="dropdown__menu__item" onClick={this.openMyProfile}>
                  <i className="material-icons">person</i>
                  Edit Profile
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item">
                  <i className="material-icons">power</i>
                  Configure Integrations
                </li>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  Help &amp; Feedback
                </li>
                <li className="dropdown__menu__item">
                  <i className="material-icons">settings</i>
                  Preferences
                </li>
                <li className="dropdown__menu__item dropdown__menu__item--light" onClick={this.setLogout}>
                  Sign Out
                </li>
              </ul>
            </div>
          </div>

          <MyProfileModal/>
        </header>
      );
    } else {
      return null;
    }
  }
}

export default HeaderSection;
