/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';
import { KeyCodes } from '../../constants/ActorAppConstants';

import ContactActionCreators from '../../actions/ContactActionCreators';
import GroupListActionCreators from '../../actions/GroupListActionCreators';

import ContactStore from '../../stores/PeopleStore';
import GroupListStore from '../../stores/GroupListStore';

import PeopleList from './PeopleList'
import GroupList from './GroupList'

class ModalsWrapper extends Component {
  constructor(props) {
    super(props);
  }

  static getStores = () => [ContactStore, GroupListStore];

  static calculateState() {
    return {
      isPeoplesOpen: ContactStore.isOpen(),
      isGroupsOpen: GroupListStore.isOpen()
    };
  }

  static contextTypes = {
    intl: PropTypes.object
  };

  componentWillMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);

  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleKeyDown = (event) => {
    switch (event.keyCode) {
      case KeyCodes.ESC:
        event.preventDefault();
        this.handleClose();
        break;
      case KeyCodes.G:
        if (event.ctrlKey) {
          event.preventDefault();
          this.handleClose();
          GroupListActionCreators.open();
        }
        break;
      case KeyCodes.P:
        if (event.ctrlKey) {
          event.preventDefault();
          this.handleClose();
          ContactActionCreators.open();
        }
        break;
      default:
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
    const { intl } = this.context;

    const wrapperClassName = classnames('modal-wrapper', {
      'modal-wrapper--opened': isPeoplesOpen || isGroupsOpen
    });

    return (
      <div className={wrapperClassName}>
        <div className="modal-wrapper__close" onClick={this.handleClose}>
          <i className="close_icon material-icons">close</i>
          <div className="text">{intl.messages['button.close']}</div>
        </div>

        {isPeoplesOpen ? <PeopleList/> : null}
        {isGroupsOpen ? <GroupList/> : null}
      </div>
    );
  }
}

export default Container.create(ModalsWrapper, { pure: false });
