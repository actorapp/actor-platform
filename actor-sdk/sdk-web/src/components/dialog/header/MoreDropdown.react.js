/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classnames from 'classnames';
import EventListener from 'fbjs/lib/EventListener';
import { PeerTypes, KeyCodes } from '../../../constants/ActorAppConstants';

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
    onClose(event)
  }

  handleKeyDown(event) {
    const { onClose } = this.props;

    if (event.keyCode === KeyCodes.ESC) {
      event.stopPropagation();
      event.preventDefault();
      onClose(event)
    }
  }

  renderToggleContact() {
    const { info: { isContact } } = this.props;

    if (isContact) {
      return (
        <li className="dropdown__menu__item">
          <FormattedMessage id="removeFromContacts"/>
        </li>
      );
    }

    return (
      <li className="dropdown__menu__item">
        <FormattedMessage id="addToContacts"/>
      </li>
    );
  }

  renderBlockUser() {
    if (!this.context.delegate.features.blocking) {
      return null;
    }

    return (
      <li className="dropdown__menu__item">
        <FormattedMessage id="blockUser"/>
      </li>
    );
  }

  renderUserMenuItems() {
    return (
      <div>
        {this.renderToggleContact()}
        {this.renderBlockUser()}
        <li className="dropdown__menu__item">
          <FormattedMessage id="clearConversation"/>
        </li>
        <li className="dropdown__menu__item">
          <FormattedMessage id="deleteConversation"/>
        </li>
      </div>
    );
  }

  renderGroupMenuItems() {
    return (
      <div>
        <li className="dropdown__menu__item">
          <i className="material-icons">mode_edit</i>
          <FormattedMessage id="editGroup"/>
        </li>
        <li className="dropdown__menu__item">
          <i className="material-icons">person_add</i>
          <FormattedMessage id="addPeople"/>
        </li>
        <li className="dropdown__menu__item">
          <FormattedMessage id="leaveGroup"/>
        </li>
        <li className="dropdown__menu__item">
          <FormattedMessage id="clearGroup"/>
        </li>
        <li className="dropdown__menu__item">
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
        {this.renderMediaMenuItems()}
        {this.renderPeerMenuItems()}
      </ul>
    );
  }
}

export default MoreDropdown;
