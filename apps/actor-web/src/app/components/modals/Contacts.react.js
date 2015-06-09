import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';
import ContactStore from '../../stores/ContactStore';

import Modal from 'react-modal';
import AvatarItem from '../common/AvatarItem.react';

var appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

var getStateFromStores = function() {
  return {
    contacts: ContactStore.getContacts(),
    isShown: ContactStore.isContactsOpen()
  };
};

var Contacts = React.createClass({
  getInitialState() {
    return (getStateFromStores());
  },

  componentWillMount() {
    ContactStore.addChangeListener(this._onChange);
  },

  componentWillUnmount() {
    ContactStore.removeChangeListener(this._onChange);
  },

  render() {
    var contacts = this.state.contacts;
    var isShown = this.state.isShown;


    var contactList = _.map(contacts, function(contact, i) {
      return (
        <Contacts.ContactItem key={i} contact={contact}/>
      );
    });

    if (contacts !== null) {
      return (
        <Modal closeTimeoutMS={150}
               isOpen={isShown} className="modal contacts">

          <header className="modal__header">
            <a className="modal__header__close material-icons" onClick={this._onClose}>clear</a>
            <h3>Contact list</h3>
          </header>

          <div className="modal__body">
            <ul className="contacts__list">
              {contactList}
            </ul>
          </div>
        </Modal>
      );
    } else {
      return (null);
    }
  },

  _onChange() {
    this.setState(getStateFromStores());
  },

  _onClose() {
    ContactActionCreators.hideContactList();
  }
});

Contacts.ContactItem = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    contact: React.PropTypes.object
  },

  render() {
    var contact = this.props.contact;

    return (
      <li className="contacts__list__item row">
        <AvatarItem title={contact.name}
                    image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"/>

        <div className="col-xs">
          <span className="title">
            {contact.name}
          </span>
        </div>

        <div className="controls">
          <a className="material-icons" onClick={this._openNewPrivateCoversation}>message</a>
        </div>
      </li>
    );
  },

  _openNewPrivateCoversation() {
    DialogActionCreators.selectDialogPeerUser(this.props.contact.uid);
    ContactActionCreators.hideContactList();
  }
});


export default Contacts;
