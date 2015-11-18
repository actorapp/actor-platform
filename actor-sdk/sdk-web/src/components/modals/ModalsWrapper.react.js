/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';
import { KeyCodes } from '../../constants/ActorAppConstants';
import ReactMixin from 'react-mixin';
import { IntlMixin } from 'react-intl';

import ContactActionCreators from '../../actions/ContactActionCreators';
import GroupListActionCreators from '../../actions/GroupListActionCreators';

import ContactsStore from '../../stores/ContactStore';
import ContactStore from '../../stores/ContactStore';
import GroupListStore from '../../stores/GroupListStore';

import Contacts from './NewContacts.react'
import Groups from './GroupList'

const getStates = () => {
  return {
    isContactsOpen: ContactsStore.isContactsOpen(),
    isGroupsOpen: GroupListStore.isGroupsOpen()
  }
};

class ModalsWrapper extends Component {
  constructor(props) {
    super(props);

    this.state = getStates();
  }

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);

    ContactsStore.addChangeListener(this.handleChange);
    GroupListStore.addListener(this.handleChange);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);

    ContactsStore.removeChangeListener(this.handleChange);
  }

  handleChange = () => this.setState(getStates());

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  handleClose = () => {
    const { isContactsOpen, isGroupsOpen } = this.state;

    if (isContactsOpen) {
      ContactActionCreators.close();
    }
    if (isGroupsOpen) {
      GroupListActionCreators.close();
    }
  };

  render() {
    const { isContactsOpen, isGroupsOpen } = this.state;

    const wrapperClassName = classnames('modal-wrapper', {
      'modal-wrapper--opened': isContactsOpen || isGroupsOpen
    });

    return (
      <div className={wrapperClassName}>
        <div className="modal-wrapper__close" onClick={this.handleClose}>
          <i className="close_icon material-icons">close</i>
          <div className="text">{this.getIntlMessage('button.close')}</div>
        </div>

        {isContactsOpen ? <Contacts/> : null}
        {isGroupsOpen ? <Groups/> : null}
      </div>
    );
  }
}

ReactMixin.onClass(ModalsWrapper, IntlMixin);

export default ModalsWrapper;
