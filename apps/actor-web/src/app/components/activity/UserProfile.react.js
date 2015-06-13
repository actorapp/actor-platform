import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';

import AvatarItem from '../common/AvatarItem.react';

var UserProfile = React.createClass({
  propTypes: {
    user: React.PropTypes.object.isRequired
  },

  mixins: [PureRenderMixin],

  _addToContacts() {
    ContactActionCreators.addContact(this.props.user.id);
  },

  _removeFromContacts() {
    ContactActionCreators.removeContact(this.props.user.id);
  },

  render() {
    let user = this.props.user;

    let addToContacts;

    if (user.isContact === false) {
      addToContacts = <a className="button button--wide" onClick={this._addToContacts}>Add to contacts</a>;
    } else {
      addToContacts = <a className="button button--wide" onClick={this._removeFromContacts}>Remove from contacts</a>;
    }

    return (
      <div className="activity__body profile">
        <AvatarItem image={user.bigAvatar}
                    placeholder={user.placeholder}
                    size="huge"
                    title={user.name}/>

        <h3 className="profile__name">{user.name}</h3>

        <UserProfile.ContactInfo phones={user.phones}/>

        <footer className="profile__controls">
          {addToContacts}
        </footer>
      </div>
    );
  }
});

UserProfile.ContactInfo = React.createClass({
  propTypes: {
    phones: React.PropTypes.array
  },

  mixins: [PureRenderMixin],

  render: function () {
    let phones = this.props.phones;

    let contactPhones = _.map(phones, (phone, i) => {
      return (
        <li className="profile__list__item row" key={i}>
          <i className="material-icons">call</i>
          <div className="col-xs">
            <span className="contact">+{phone.number}</span>
            <span className="title">{phone.title}</span>
          </div>
        </li>
      );
    });

    return (
      <ul className="profile__list profile__list--contacts">
        {contactPhones}
      </ul>
    );
  }
});

export default UserProfile;
