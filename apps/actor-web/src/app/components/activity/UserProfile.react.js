import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';

import AvatarItem from '../common/AvatarItem.react';

var UserProfile = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    user: React.PropTypes.object.isRequired
  },

  render: function() {
    var user = this.props.user;

    var addToContacts;

    if (user.isContact === false) {
      addToContacts = <a className="button button--wide hide" onClick={this._addToContacts}>Add to contacts</a>;
    } else {
      addToContacts = <a className="button button--wide hide" onClick={this._removeFromContacts}>Remove from contacts</a>;
    }

    return (
      <div className="activity__body profile">
        <AvatarItem title={user.name}
                    image={user.bigAvatar}
                    placeholder={user.placeholder}
                    size="huge"/>

        <h3 className="profile__name">{user.name}</h3>

        <UserProfile.ContactInfo phones={user.phones}/>

        <footer className="profile__controls">
          {addToContacts}
        </footer>
      </div>
    );
  },

  _addToContacts: function() {
    //console.warn('_addToContacts');
    ContactActionCreators.addContact(this.props.user.id);
  },

  _removeFromContacts: function() {
    //console.warn('_removeFromContacts');
    ContactActionCreators.removeContact(this.props.user.id);
  }
});

UserProfile.ContactInfo = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    phones: React.PropTypes.array
  },

  render: function () {
    var phones = this.props.phones;

    var contactPhones = _.map(phones, function(phone, i) {
      return (
        <li key={i} className="profile__list__item row">
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
