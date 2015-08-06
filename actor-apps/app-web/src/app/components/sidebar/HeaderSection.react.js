import React from 'react';
import mixpanel from 'utils/Mixpanel';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import classNames from 'classnames';

import MyProfileActions from 'actions/MyProfileActions';
import LoginActionCreators from 'actions/LoginActionCreators';
import HelpActionCreators from 'actions/HelpActionCreators';
import AddContactActionCreators from 'actions/AddContactActionCreators';

import AvatarItem from 'components/common/AvatarItem.react';
import MyProfileModal from 'components/modals/MyProfile.react';
import ActorClient from 'utils/ActorClient';

import AddContactModal from 'components/modals/AddContact.react';

import PreferencesModal from '../modals/Preferences.react';
import PreferencesActionCreators from 'actions/PreferencesActionCreators';

var getStateFromStores = () => {
  return {
    dialogInfo: null,
    isOpened: false
  };
};

@ReactMixin.decorate(IntlMixin)
class HeaderSection extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();
  }

  componentDidMount() {
    ActorClient.bindUser(ActorClient.getUid(), this.setUser);
  }

  componentWillUnmount() {
    ActorClient.unbindUser(ActorClient.getUid(), this.setUser);
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

  openAddContactModal = () => {
    AddContactActionCreators.openModal();
  };

  onSettingsOpen = () => {
    PreferencesActionCreators.show();
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
                <li className="dropdown__menu__item hide">
                  <i className="material-icons">photo_camera</i>
                  <FormattedMessage message={this.getIntlMessage('setProfilePhoto')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openMyProfile}>
                  <i className="material-icons">edit</i>
                  <FormattedMessage message={this.getIntlMessage('editProfile')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openAddContactModal}>
                  <i className="material-icons">person_add</i>
                  Add contact
                </li>
                <li className="dropdown__menu__separator"></li>
                <li className="dropdown__menu__item  hide">
                  <svg className="icon icon--dropdown"
                       dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#integration"/>'}}/>
                  <FormattedMessage message={this.getIntlMessage('configureIntegrations')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.openHelpDialog}>
                  <i className="material-icons">help</i>
                  <FormattedMessage message={this.getIntlMessage('helpAndFeedback')}/>
                </li>
                <li className="dropdown__menu__item" onClick={this.onSettingsOpen}>
                  <i className="material-icons">settings</i>
                  <FormattedMessage message={this.getIntlMessage('preferences')}/>
                </li>
                <li className="dropdown__menu__item dropdown__menu__item--light" onClick={this.setLogout}>
                  <FormattedMessage message={this.getIntlMessage('signOut')}/>
                </li>
              </ul>
            </div>
          </div>

          <MyProfileModal/>
          <AddContactModal/>
          <PreferencesModal/>
        </header>
      );
    } else {
      return null;
    }
  }
}

export default HeaderSection;
