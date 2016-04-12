/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { ModalTypes } from '../../constants/ActorAppConstants';

import ModalStore from '../../stores/ModalStore';

import Profile from './Profile.react';

class ModalsWrapper extends Component {
  static getStores() {
    return [ModalStore];
  }

  static calculateState() {
    return ModalStore.getState();
  }

  render() {
    const { current } = this.state;
    if (!current) return null;

    switch (current) {
      case ModalTypes.PROFILE:
        return <Profile/>;
      default:
        console.warn(`Unsupported modal type: ${current}`);
        return null;
    }
  }
}

export default Container.create(ModalsWrapper);
