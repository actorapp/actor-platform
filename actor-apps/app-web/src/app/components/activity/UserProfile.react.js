import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';

import PeerStore from '../../stores/PeerStore';
import DialogStore from '../../stores/DialogStore';

import AvatarItem from '../common/AvatarItem.react';
import UserProfileContactInfo from './UserProfileContactInfo.react';

const getStateFromStores = (userId) => {
  const thisPeer = PeerStore.getUserPeer(userId);
  return {
    thisPeer: thisPeer,
    isNotificationsEnabled: DialogStore.isNotificationsEnabled(thisPeer)
  };
};

var UserProfile = React.createClass({
  propTypes: {
    user: React.PropTypes.object.isRequired
  },

  mixins: [PureRenderMixin],

  getInitialState() {
    return getStateFromStores(this.props.user.id);
  },

  componentWillMount() {
    DialogStore.addNotificationsListener(this.whenNotificationChanged);
  },

  componentWillUnmount() {
    DialogStore.removeNotificationsListener(this.whenNotificationChanged);
  },

  _addToContacts() {
    ContactActionCreators.addContact(this.props.user.id);
  },

  _removeFromContacts() {
    ContactActionCreators.removeContact(this.props.user.id);
  },

  onNotificationChange(event) {
    DialogActionCreators.changeNotificationsEnabled(this.state.thisPeer, event.target.checked);
  },

  whenNotificationChanged() {
    this.setState(getStateFromStores(this.props.user.id));
  },

  render() {
    const user = this.props.user;
    const isNotificationsEnabled = this.state.isNotificationsEnabled;

    let addToContacts;

    if (user.isContact === false) {
      addToContacts = <a onClick={this._addToContacts}>Add to contacts</a>;
    } else {
      addToContacts = <a className="red" onClick={this._removeFromContacts}>Remove from contacts</a>;
    }

    return (
      <div className="activity__body profile">

        <div className="profile__name">
          <AvatarItem image={user.bigAvatar}
                      placeholder={user.placeholder}
                      size="medium"
                      title={user.name}/>
          <h3>{user.name}</h3>
        </div>

        <div className="notifications">
          <label htmlFor="notifications">Enable Notifications</label>

          <div className="switch pull-right">
            <input checked={isNotificationsEnabled} id="notifications" onChange={this.onNotificationChange} type="checkbox"/>
            <label htmlFor="notifications"></label>
          </div>
        </div>

        <UserProfileContactInfo phones={user.phones}/>

        <ul className="profile__list profile__list--usercontrols">
          <li className="profile__list__item">
            {addToContacts}
          </li>
        </ul>
      </div>
    );
  }
});

export default UserProfile;
