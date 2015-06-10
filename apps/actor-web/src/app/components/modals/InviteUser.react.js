import _ from 'lodash';

import React from 'react';

import ActorClient from '../../utils/ActorClient';

import InviteUserActions from '../../actions/InviteUserActions';

import ContactStore from '../../stores/ContactStore';
import InviteUserStore from '../../stores/InviteUserStore';

import Modal from 'react-modal';

import ContactItem from './invite-user/ContactItem.react';

const getStateFromStores = function () {
  return ({
    contacts: ContactStore.getContacts(),
    group: InviteUserStore.getGroup(),
    inviteUrl: InviteUserStore.getInviteUrl(),
    isOpen: InviteUserStore.isModalOpen()
  });
};

const hasMember = (group, userId) =>
undefined !== _.find(group.members, (c) => c.peerInfo.peer.id === userId);

export default React.createClass({
  getInitialState () {
    return getStateFromStores();
  },

  componentWillMount() {
    this.unsubscribe = InviteUserStore.listen(this._onChange);
    ContactStore.addChangeListener(this._onChange);
  },

  componentWillUnmount() {
    this.unsubscribe();
    ContactStore.removeChangeListener(this._onChange);
  },

  render () {
    let contacts = this.state.contacts;
    let isOpen = this.state.isOpen;

    if (isOpen) {
      let contactList = [];

      _.forEach(contacts, function (contact, i) {
        if (!hasMember(this.state.group, contact.uid)) {
          contactList.push(
            <ContactItem key={i} contact={contact} onSelect={this._onContactSelect}/>
          );
        }
      }, this);

      let inviteViaUrl = null;

      if (this.state.inviteUrl) {
        inviteViaUrl =
          <li className="contacts__list__item row">
            <div className="col-xs-4">
              <div className="box">Or send a link:</div>
            </div>
            <div className="col-xs-6">
              <div className="box">
                <input readOnly value={this.state.inviteUrl} onClick={this._onInviteUrlClick}/>
              </div>
            </div>
          </li>;
      }

      return (
        <Modal closeTimeoutMS={150}
               isOpen={isOpen} className="modal modal--invite contacts">

          <header className="modal__header">
            <a className="modal__header__close material-icons" onClick={this._onClose}>clear</a>

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
  },

  _onChange () {
    this.setState(getStateFromStores());
  },

  _onClose () {
    InviteUserActions.modalClose();
  },

  _onContactSelect (contact) {
    ActorClient.inviteMember(this.state.group.id, contact.uid)
      .then(() => InviteUserActions.modalClose());
  },

  _onInviteUrlClick (event) {
    event.target.select();
  }
});

