/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import classnames from 'classnames';

import MessageActions from './dropdown/MessageActions.react'

import DropdownStore from '../../stores/DropdownStore'
import DialogStore from '../../stores/DialogStore'

class DropdownWrapper extends Component {
  static getStores = () => [DropdownStore, DialogStore];

  static calculateState() {
    const message = DropdownStore.getMessage();

    return {
      isOpen: DropdownStore.isOpen(message.rid),
      peer: DialogStore.getCurrentPeer(),
      targetRect: DropdownStore.getTargetRect(),
      message
    };
  }

  constructor(props) {
    super(props);
  }

  render() {
    const { isOpen, message, targetRect, peer } = this.state;

    const dropdownWrapperClassName = classnames('dropdown-wrapper', {
      'dropdown-wrapper--opened': isOpen
    });

    return (
      <div className={dropdownWrapperClassName}>
        {
          isOpen
            ? <MessageActions message={message}
                              targetRect={targetRect}
                              peer={peer}
                              hideOnScroll={true}/>
            : null
        }
      </div>
    );
  }
}

export default Container.create(DropdownWrapper, {pure: false});
