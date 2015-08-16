import _ from 'lodash';

import React from 'react';
import Modal from 'react-modal';
import ReactMixin from 'react-mixin';
import { IntlMixin, FormattedMessage } from 'react-intl';
import { Styles, FlatButton } from 'material-ui';
import ActorTheme from 'constants/ActorTheme';

import ActorClient from 'utils/ActorClient';
import { KeyCodes } from 'constants/ActorAppConstants';

import InviteUserActions from 'actions/InviteUserActions';
import InviteUserByLinkActions from 'actions/InviteUserByLinkActions';

import ContactStore from 'stores/ContactStore';
import InviteUserStore from 'stores/InviteUserStore';

import ContactItem from './invite-user/ContactItem.react';

const ThemeManager = new Styles.ThemeManager();

const getStateFromStores = () => {
  return ({
    contacts: ContactStore.getContacts(),
    group: InviteUserStore.getGroup(),
    isOpen: InviteUserStore.isModalOpen()
  });
};

const hasMember = (group, userId) =>
  undefined !== _.find(group.members, (c) => c.peerInfo.peer.id === userId);

@ReactMixin.decorate(IntlMixin)
class InviteUser extends React.Component {
  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  constructor(props) {
    super(props);

    this.state = _.assign({
      search: ''
    }, getStateFromStores());

    ThemeManager.setTheme(ActorTheme);
    ThemeManager.setComponentThemes({
      button: {
        minWidth: 60
      }
    });

    InviteUserStore.addChangeListener(this.onChange);
    ContactStore.addChangeListener(this.onChange);
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    InviteUserStore.removeChangeListener(this.onChange);
    ContactStore.removeChangeListener(this.onChange);
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  onClose = () => {
    InviteUserActions.hide();
  };

  onContactSelect = (contact) => {
    ActorClient.inviteMember(this.state.group.id, contact.uid);
  };

  onInviteUrlByClick = () => {
    InviteUserByLinkActions.show(this.state.group);
    InviteUserActions.hide();
  };

  onKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClose();
    }
  };

  onSearchChange = (event) => {
    this.setState({search: event.target.value});
  };

  render() {
    const contacts = this.state.contacts;
    const isOpen = this.state.isOpen;

    let contactList = [];
    if (isOpen) {

      _.forEach(contacts, (contact, i) => {
        const name = contact.name.toLowerCase();
        if (name.includes(this.state.search.toLowerCase())) {
          if (!hasMember(this.state.group, contact.uid)) {
            contactList.push(
              <ContactItem contact={contact} key={i} onSelect={this.onContactSelect}/>
            );
          } else {
            contactList.push(
              <ContactItem contact={contact} key={i} member/>
            );
          }
        }
      }, this);
    }

    if (contactList.length === 0) {
      contactList.push(
        <li className="contacts__list__item contacts__list__item--empty text-center">
          <FormattedMessage message={this.getIntlMessage('inviteModalNotFound')}/>
        </li>
      );
    }

    return (
      <Modal className="modal-new modal-new--invite contacts"
             closeTimeoutMS={150}
             isOpen={isOpen}
             style={{width: 400}}>

        <header className="modal-new__header">
          <a className="modal-new__header__icon material-icons">person_add</a>
          <h4 className="modal-new__header__title">
            <FormattedMessage message={this.getIntlMessage('inviteModalTitle')}/>
          </h4>
          <div className="pull-right">
            <FlatButton hoverColor="rgba(74,144,226,.12)"
                        label="Done"
                        labelStyle={{padding: '0 8px'}}
                        onClick={this.onClose}
                        secondary={true}
                        style={{marginTop: -6}}/>
          </div>
        </header>

        <div className="modal-new__body">
          <div className="modal-new__search">
            <i className="material-icons">search</i>
            <input className="input input--search"
                   onChange={this.onSearchChange}
                   placeholder={this.getIntlMessage('inviteModalSearch')}
                   type="search"
                   value={this.state.search}/>
          </div>

          <a className="link link--blue" onClick={this.onInviteUrlByClick}>
            <i className="material-icons">link</i>
            {this.getIntlMessage('inviteByLink')}
          </a>
        </div>

        <div className="contacts__body">
          <ul className="contacts__list">
            {contactList}
          </ul>
        </div>

      </Modal>
    );
  }
}

export default InviteUser;
