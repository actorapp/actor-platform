/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage, FormattedHTMLMessage } from 'react-intl';
import EventListener from 'fbjs/lib/EventListener';
import { PeerTypes, KeyCodes } from '../../../constants/ActorAppConstants';
import confirm from '../../../utils/confirm';

import ContactActionCreators from '../../../actions/ContactActionCreators';
import DialogActionCreators from '../../../actions/DialogActionCreators';
import BlockedUsersActionCreators from '../../../actions/BlockedUsersActionCreators';
import EditGroupActionCreators from '../../../actions/EditGroupActionCreators';
import InviteUserActions from '../../../actions/InviteUserActions';

class MoreDropdown extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  }

  static propTypes = {
    peer: PropTypes.object.isRequired,
    info: PropTypes.object.isRequired,
    onClose: PropTypes.func.isRequired
  }

  constructor(props, context) {
    super(props, context);
    console.debug(this.props);

    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleDocumentClick = this.handleDocumentClick.bind(this);
    this.handleChatClear = this.handleChatClear.bind(this);
    this.handleChatDelete = this.handleChatDelete.bind(this);
    this.handleChatLeave = this.handleChatLeave.bind(this);
    this.handleRemoveFromContacts = this.handleRemoveFromContacts.bind(this);
    this.handleAddToContacts = this.handleAddToContacts.bind(this);
    this.handleBlockUser = this.handleBlockUser.bind(this);
    this.handleUnlockUser = this.handleUnlockUser.bind(this);
    this.handleEditGroup = this.handleEditGroup.bind(this);
    this.handleAddPeople = this.handleAddPeople.bind(this);
  }

  componentDidMount() {
    this.setListeners();
  }

  componentWillUnmount() {
    this.cleanListeners();
  }

  setListeners() {
    this.cleanListeners();
    this.listeners = [
      EventListener.listen(document, 'keydown', this.handleKeyDown),
      EventListener.listen(document, 'click', this.handleDocumentClick)
    ];
  }

  cleanListeners() {
    if (this.listeners) {
      this.listeners.forEach((listener) => listener.remove());
      this.listeners = null;
    }
  }

  handleDocumentClick(event) {
    const { onClose } = this.props;
    onClose(event);
  }

  handleKeyDown(event) {
    const { onClose } = this.props;

    if (event.keyCode === KeyCodes.ESC) {
      event.stopPropagation();
      event.preventDefault();
      onClose(event)
    }
  }

  handleChatClear() {
    console.debug('handleChatClear');
    const { info, peer } = this.props;

    const message = peer.key === PeerTypes.USER
      ? <FormattedHTMLMessage id="modal.confirm.user.clear" values={{ name: info.name }} />
      : <FormattedHTMLMessage id="modal.confirm.group.clear" values={{ name: info.name }} />

    confirm(message)
      .then(
        () => DialogActionCreators.clearChat(peer),
        () => {}
      );
  }

  handleChatDelete() {
    console.debug('handleChatDelete');
    const { info, peer } = this.props;

    const message = peer.key === PeerTypes.USER
      ? <FormattedHTMLMessage id="modal.confirm.user.delete" values={{ name: info.name }} />
      : <FormattedHTMLMessage id="modal.confirm.group.delete" values={{ name: info.name }} />

    confirm(message)
      .then(
        () => DialogActionCreators.deleteChat(peer),
        () => {}
      );
  }

  handleChatLeave() {
    console.debug('handleChatLeave');
    const { peer, info } = this.props;

    confirm(<FormattedHTMLMessage id="modal.confirm.group.leave" values={{ name: info.name }} />)
      .then(
        () => DialogActionCreators.leaveGroup(peer.id),
        () => {}
      );
  }

  handleRemoveFromContacts() {
    const { peer, info } = this.props;

    confirm(<FormattedHTMLMessage id="modal.confirm.user.removeContact" values={{ name: info.name }}/>)
      .then(
        () => ContactActionCreators.removeContact(peer.id),
        () => {}
      );
  }

  handleAddToContacts() {
    const { peer } = this.props;
    ContactActionCreators.addContact(peer.id);
  }

  handleBlockUser() {
    const { peer, info } = this.props;

    confirm(<FormattedHTMLMessage id="modal.confirm.user.block" values={{ name: info.name }} />)
      .then(
        () => BlockedUsersActionCreators.blockUser(peer.id),
        () => {}
      );
  }

  handleUnlockUser() {
    const { peer } = this.props;
    BlockedUsersActionCreators.unblockUser(peer.id);
  }

  handleEditGroup() {
    const { peer } = this.props;
    EditGroupActionCreators.show(peer.id);
  }

  handleAddPeople() {
    const { info } = this.props;
    InviteUserActions.show(info)
  }

  renderToggleContact() {
    const { info: { isContact } } = this.props;

    if (isContact) {
      return (
        <li className="dropdown__menu__item" onClick={this.handleRemoveFromContacts}>
          <FormattedMessage id="removeFromContacts"/>
        </li>
      );
    }

    return (
      <li className="dropdown__menu__item" onClick={this.handleAddToContacts}>
        <FormattedMessage id="addToContacts"/>
      </li>
    );
  }

  renderBlockUser() {
    if (!this.context.delegate.features.blocking) {
      return null;
    }

    const { info: { isBlocked } } = this.props;

    if (isBlocked) {
      return (
        <li className="dropdown__menu__item" onClick={this.handleUnlockUser}>
          <FormattedMessage id="compose.unblock" />
        </li>
      );
    }

    return (
      <li className="dropdown__menu__item" onClick={this.handleBlockUser}>
        <FormattedMessage id="blockUser"/>
      </li>
    );
  }

  renderUserMenuItems() {
    return (
      <div>
        {this.renderToggleContact()}
        {this.renderBlockUser()}
        <li className="dropdown__menu__item" onClick={this.handleChatClear}>
          <FormattedMessage id="clearConversation"/>
        </li>
        <li className="dropdown__menu__item" onClick={this.handleChatDelete}>
          <FormattedMessage id="deleteConversation"/>
        </li>
      </div>
    );
  }

  renderGroupMenuItems() {
    return (
      <div>
        <li className="dropdown__menu__item" onClick={this.handleEditGroup}>
          <i className="material-icons">mode_edit</i>
          <FormattedMessage id="editGroup"/>
        </li>
        {/*<li className="dropdown__menu__item" onClick={this.handleAddPeople}>
          <i className="material-icons">group_add</i>
          <FormattedMessage id="addPeople"/>
        </li>*/}
        <li className="dropdown__menu__item" onClick={this.handleChatLeave}>
          <FormattedMessage id="leaveGroup"/>
        </li>
        <li className="dropdown__menu__item" onClick={this.handleChatClear}>
          <FormattedMessage id="clearGroup"/>
        </li>
        <li className="dropdown__menu__item" onClick={this.handleChatDelete}>
          <FormattedMessage id="deleteGroup"/>
        </li>
      </div>
    );
  }

  renderPeerMenuItems() {
    const { peer } = this.props;

    switch (peer.type) {
      case PeerTypes.USER:
        return this.renderUserMenuItems()
      case PeerTypes.GROUP:
        return this.renderGroupMenuItems()
      default:
        return null;
    }
  }

  renderMediaMenuItems() {
    return (
      <div>
        <li className="dropdown__menu__item">
          <i className="material-icons">photo</i>
          Find Photos
        </li>
        <li className="dropdown__menu__item">
          <i className="material-icons">link</i>
          Find Links
        </li>
        <li className="dropdown__menu__item">
          <i className="material-icons">insert_drive_file</i>
          Find Documents
        </li>
      </div>
    );
  }

  render() {
    return (
      <ul className="dropdown__menu dropdown__menu--right" style={{ top: 42 }}>
        {/*{this.renderMediaMenuItems()}*/}
        {this.renderPeerMenuItems()}
      </ul>
    );
  }
}

export default MoreDropdown;
