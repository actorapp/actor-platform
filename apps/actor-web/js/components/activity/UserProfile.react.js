'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var AvatarItem = require('../common/AvatarItem.react');

var UserProfile = React.createClass({
  propTypes: {
    user: React.PropTypes.object.isRequired
  },

  render: function() {
    var user = this.props.user;

    var addToContacts;

    if (user.isContact == false) {
      addToContacts = <a onClick={this._addToContacts} className="button">Add to contacts</a>;
    } else {
      addToContacts = <a onClick={this._removeFromContacts} className="button">Remove from contacts</a>;
    }

    return(
      <div className="activity__body">
        <AvatarItem title={user.name}
                    image={user.bigAvatar}
                    placeholder={user.placeholder}
                    size="huge"/>

        <h3>{user.name}</h3>

        <UserProfile.ContactInfo phones={user.phones}/>

        {addToContacts}
      </div>
    );
  },

  _addToContacts: function() {
    console.warn('_addToContacts');
  },

  _removeFromContacts: function() {
    console.warn('_removeFromContacts');
  }
});

UserProfile.ContactInfo = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    phones: React.PropTypes.array.isRequired
  },

  render: function () {
    var phones = this.props.phones;

    var contactInfo = _.map(phones, function(phone) {
      return (
        <li className="row">
          <i className="material-icons">call</i>
          <div className="col-xs">
            +{phone.number}
            <span className="title">{phone.title}</span>
          </div>
        </li>
      );
    });

    return (
      <ul className="activity__body__list activity__body__list--info">
        {contactInfo}
      </ul>
    );
  }
});

module.exports = UserProfile;
