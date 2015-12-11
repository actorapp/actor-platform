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

import ContactStore from '../../stores/PeopleStore';
import GroupListStore from '../../stores/GroupListStore';

import PeopleList from './PeopleList'
import GroupList from './GroupList'

const getStates = () => {
  return {
    isPeoplesOpen: ContactStore.isOpen(),
    isGroupsOpen: GroupListStore.isOpen()
  }
};

class ModalsWrapper extends Component {
  constructor(props) {
    super(props);

    this.state = getStates();
  }

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);

    ContactStore.addListener(this.handleChange);
    GroupListStore.addListener(this.handleChange);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleChange = () => this.setState(getStates());

  handleKeyDown = (event) => {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleClose();
    }
  };

  handleClose = () => {
    const { isPeoplesOpen, isGroupsOpen } = this.state;

    if (isPeoplesOpen) {
      ContactActionCreators.close();
    }
    if (isGroupsOpen) {
      GroupListActionCreators.close();
    }
  };

  render() {
    const { isPeoplesOpen, isGroupsOpen } = this.state;

    const wrapperClassName = classnames('modal-wrapper', {
      'modal-wrapper--opened': isPeoplesOpen || isGroupsOpen
    });

    return (
      <div className={wrapperClassName}>
        <div className="modal-wrapper__close" onClick={this.handleClose}>
          <i className="close_icon material-icons">close</i>
          <div className="text">{this.getIntlMessage('button.close')}</div>
        </div>

        {isPeoplesOpen ? <PeopleList/> : null}
        {isGroupsOpen ? <GroupList/> : null}
      </div>
    );
  }
}

ReactMixin.onClass(ModalsWrapper, IntlMixin);

export default ModalsWrapper;
