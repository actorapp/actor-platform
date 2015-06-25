import _ from 'lodash';

import React from 'react';

import ActorClient from '../../utils/ActorClient';

import InviteUserActions from '../../actions/InviteUserActions';

import ContactStore from '../../stores/ContactStore';
import InviteUserStore from '../../stores/InviteUserStore';

import Modal from 'react-modal';

import ContactItem from './invite-user/ContactItem.react';

const getStateFromStores = () => {
  return ({
    contacts: ContactStore.getContacts(),
    group: InviteUserStore.getGroup(),
    inviteUrl: InviteUserStore.getInviteUrl(),
    isOpen: InviteUserStore.isModalOpen()
  });
};

const hasMember = (group, userId) =>
  undefined !== _.find(group.members, (c) => c.peerInfo.peer.id === userId);

class InviteUser extends React.Component {
  constructor() {
    super();

    this.onChange = this.onChange.bind(this);
    this.onClose = this.onClose.bind(this);
    this.onContactSelect = this.onContactSelect.bind(this);
    this.onInviteUrlClick = this.onInviteUrlClick.bind(this);

    this.state = getStateFromStores();
  }

  componentWillMount() {
    this.unsubscribe = InviteUserStore.listen(this.onChange);
    ContactStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onClose, false);
  }

  componentWillUnmount() {
    this.unsubscribe();
    ContactStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onClose, false);
  }

  onChange() {
    this.setState(getStateFromStores());
  }

  onClose() {
    InviteUserActions.modalClose();
  }

  onContactSelect(contact) {
    ActorClient.inviteMember(this.state.group.id, contact.uid)
      .then(() => InviteUserActions.modalClose());
  }

  onInviteUrlClick(event) {
    event.target.select();
  }


  render() {
    let contacts = this.state.contacts;
    let isOpen = this.state.isOpen;

    if (isOpen) {
      let contactList = [];

      _.forEach(contacts, function (contact, i) {
        if (!hasMember(this.state.group, contact.uid)) {
          contactList.push(
            <ContactItem contact={contact} key={i} onSelect={this.onContactSelect}/>
          );
        }
      }, this);

      let inviteViaUrl = null;

      if (this.state.inviteUrl) {
        inviteViaUrl = (
          <li className="contacts__list__item row">
            <div className="col-xs-4">
              <div className="box">Or send a link:</div>
            </div>
            <div className="col-xs-6">
              <div className="box">
                <input onClick={this.onInviteUrlClick} readOnly value={this.state.inviteUrl}/>
              </div>
            </div>
          </li>
        );
      }

      return (
        <Modal className="modal modal--invite contacts"
               closeTimeoutMS={150}
               isOpen={isOpen}>

          <header className="modal__header">
            <a className="modal__header__close material-icons" onClick={this.onClose}>clear</a>

            <h3>Select contact</h3>
          </header>

          <div className="modal__body">
            <ul className="contacts__list">
              {inviteViaUrl}
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

export default InviteUser;
