import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import ContactActionCreators from '../../actions/ContactActionCreators';
import DialogActionCreators from '../../actions/DialogActionCreators';
import ContactStore from '../../stores/ContactStore';

import Modal from 'react-modal';
import AvatarItem from '../common/AvatarItem.react';

let appElement = document.getElementById('actor-web-app');
Modal.setAppElement(appElement);

let getStateFromStores = () => {
  return {
    contacts: ContactStore.getContacts(),
    isShown: ContactStore.isContactsOpen()
  };
};

class Contacts extends React.Component {
  componentWillMount() {
    ContactStore.addChangeListener(this._onChange);
  }

  componentWillUnmount() {
    ContactStore.removeChangeListener(this._onChange);
  }

  constructor() {
    super();

    this._onClose = this._onClose.bind(this);
    this._onChange = this._onChange.bind(this);

    this.state = getStateFromStores();
  }

  _onChange() {
    this.setState(getStateFromStores());
  }

  _onClose() {
    ContactActionCreators.hideContactList();
  }


  render() {
    let contacts = this.state.contacts;
    let isShown = this.state.isShown;

    let contactList = _.map(contacts, (contact, i) => {
      return (
        <Contacts.ContactItem contact={contact} key={i}/>
      );
    });

    if (contacts !== null) {
      return (
        <Modal className="modal contacts"
               closeTimeoutMS={150}
               isOpen={isShown}>

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
  }

}

Contacts.ContactItem = React.createClass({
  propTypes: {
    contact: React.PropTypes.object
  },

  mixins: [PureRenderMixin],

  _openNewPrivateCoversation() {
    DialogActionCreators.selectDialogPeerUser(this.props.contact.uid);
    ContactActionCreators.hideContactList();
  },

  render() {
    let contact = this.props.contact;

    return (
      <li className="contacts__list__item row">
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

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
  }
});


export default Contacts;
