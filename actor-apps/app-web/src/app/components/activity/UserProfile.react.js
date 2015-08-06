import _ from 'lodash';
import React from 'react';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import classnames from 'classnames';

import ContactActionCreators from 'actions/ContactActionCreators';
import DialogActionCreators from 'actions/DialogActionCreators';

import PeerStore from 'stores/PeerStore';
import DialogStore from 'stores/DialogStore';

import AvatarItem from 'components/common/AvatarItem.react';
//import UserProfileContactInfo from 'components/activity/UserProfileContactInfo.react';
import Fold from 'components/common/Fold.React';

const getStateFromStores = (userId) => {
  const thisPeer = PeerStore.getUserPeer(userId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer)
  };
};

@ReactMixin.decorate(IntlMixin)
class UserProfile extends React.Component {
  static propTypes = {
    user: React.PropTypes.object.isRequired
  };

  constructor(props) {
    super(props);

    this.state = _.assign({
      isActionsDropdownOpen: false
    }, getStateFromStores(props.user.id));

    DialogStore.addNotificationsListener(this.onChange);
  }

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.onChange);
  }

  componentWillReceiveProps(newProps) {
    this.setState(getStateFromStores(newProps.user.id));
  }

  addToContacts = () => {
    ContactActionCreators.addContact(this.props.user.id);
  };

  removeFromContacts =() => {
    ContactActionCreators.removeContact(this.props.user.id);
  };

  onNotificationChange = (event) => {
    DialogActionCreators.changeNotificationsEnabled(this.state.thisPeer, event.target.checked);
  };

  onChange = () => {
    this.setState(getStateFromStores(this.props.user.id));
  };

  toggleActionsDropdown = () => {
    const isActionsDropdownOpen = this.state.isActionsDropdownOpen;

    if (!isActionsDropdownOpen) {
      this.setState({isActionsDropdownOpen: true});
      document.addEventListener('click', this.closeActionsDropdown, false);
    } else {
      this.closeActionsDropdown();
    }
  };

  closeActionsDropdown = () => {
    this.setState({isActionsDropdownOpen: false});
    document.removeEventListener('click', this.closeActionsDropdown, false);
  };

  render() {
    const user = this.props.user;
    const isNotificationsEnabled = this.state.isNotificationsEnabled;

    let actions;
    if (user.isContact === false) {
      actions = (
        <li className="dropdown__menu__item" onClick={this.addToContacts}>
          <FormattedMessage message={this.getIntlMessage('addToContacts')}/>
        </li>
      );
    } else {
      actions = (
        <li className="dropdown__menu__item" onClick={this.removeFromContacts}>
          <FormattedMessage message={this.getIntlMessage('removeFromContacts')}/>
        </li>
      );
    }

    let dropdownClassNames = classnames('dropdown pull-left', {
      'dropdown--opened': this.state.isActionsDropdownOpen
    });

    // Mock
    const nickname = '@username';
    const email = 'username@domain.com';

    return (
      <div className="activity__body user_profile">

        <ul className="profile__list">
          <li className="profile__list__item user_profile__meta">
            <header>
              <AvatarItem image={user.bigAvatar}
                          placeholder={user.placeholder}
                          size="big"
                          title={user.name}/>

              <h3 className="user_profile__meta__title">{user.name}</h3>
              <div className="user_profile__meta__presence">{user.presence}</div>
            </header>

            <footer>
              <div className={dropdownClassNames}>
                <button className="dropdown__button button button--light-blue" onClick={this.toggleActionsDropdown}>
                  <i className="material-icons">more_horiz</i>
                  <FormattedMessage message={this.getIntlMessage('actions')}/>
                </button>
                <ul className="dropdown__menu dropdown__menu--left">
                  {actions}
                </ul>
              </div>
            </footer>
          </li>

          <li className="profile__list__item user_profile__contact_info no-p">
            <ul className="user_profile__contact_info__list">
              <li className="hide">
                <svg className="icon icon--pink"
                     dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/sprite/icons.svg#username"/>'}}/>
                <span className="title">{nickname}</span>
                <span className="description">nickname</span>
              </li>
              <li>
                <i className="material-icons icon icon--green">call</i>
                <span className="title">{'+' + user.phones[0].number}</span>
                <span className="description">mobile</span>
              </li>
              <li className="hide">
                <i className="material-icons icon icon--blue">mail</i>
                <span className="title">{email}</span>
                <span className="description">email</span>
              </li>
            </ul>
          </li>

          <li className="profile__list__item user_profile__media no-p hide">
            <Fold icon="attach_file" iconClassName="icon--gray" title={this.getIntlMessage('sharedMedia')}>
              <ul>
                <li><a>230 Shared Photos and Videos</a></li>
                <li><a>49 Shared Links</a></li>
                <li><a>49 Shared Files</a></li>
              </ul>
            </Fold>
          </li>

          <li className="profile__list__item user_profile__notifications no-p">
            <label htmlFor="notifications">
              <i className="material-icons icon icon--squash">notifications_none</i>
              <FormattedMessage message={this.getIntlMessage('notifications')}/>
              <div className="switch pull-right">
                <input checked={isNotificationsEnabled}
                       id="notifications"
                       onChange={this.onNotificationChange}
                       type="checkbox"/>
                <label htmlFor="notifications"></label>
              </div>
            </label>
          </li>

        </ul>
      </div>
    );
  }
}

export default UserProfile;
