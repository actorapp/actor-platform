'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var ContactActionCreators = require('../actions/ContactActionCreators');
var ContactStore = require('../stores/ContactStore');

var classNames = require('classnames');
var Modal = require('react-modal');
var AvatarItem = require('./common/AvatarItem.react');

var appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

var getStateFromStores = function() {
  return({
    contacts: ContactStore.getContacts(),
    isShown: ContactStore.isContactsOpen()
  })
};

var ContactSection = React.createClass({
  getInitialState: function() {
    return (getStateFromStores());
  },

  componentWillMount: function() {
    ContactStore.addChangeListener(this._onChange);
  },

  componentWillUnmount: function() {
    ContactStore.removeChangeListener(this._onChange);
  },

  render: function() {
    var contacts = this.state.contacts;
    var isShown = this.state.isShown;


    var contactList = _.map(contacts, function(contact, i) {
      return(
        <ContactSection.Contact key={i} contact={contact}/>
      );
    });

    if (contacts !== null) {
      return(
        <Modal closeTimeoutMS={150}
               isOpen={isShown}>

          <header className="ReactModal__Content__header">
            <a className="ReactModal__Content__header__close material-icons" onClick={this._onClose}>clear</a>
            <h3>Contact list</h3>
          </header>

          <div className="ReactModal__Content__body">
            <div className="contact__list">
              {contactList}
            </div>
          </div>
        </Modal>
      );
    } else {
      return (null);
    }
  },

  _onChange: function() {
    this.setState(getStateFromStores());
  },

  _onClose: function() {
    ContactActionCreators.hideContactList();
  }
});

ContactSection.Contact = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    contact: React.PropTypes.object
  },

  render: function () {
    var contact = this.props.contact;

    return (
      <div>{contact}</div>
    );
  }
});


module.exports = ContactSection;
