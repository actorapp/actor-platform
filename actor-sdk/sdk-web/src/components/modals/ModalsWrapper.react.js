/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { ModalTypes } from '../../constants/ActorAppConstants';

import ModalStore from '../../stores/ModalStore';

import Profile from './Profile.react';
import Crop from './Crop.react';

class ModalsWrapper extends Component {
  static getStores() {
    return [ModalStore];
  }

  static calculateState() {
    return ModalStore.getState();
  }

  render() {
    console.debug(this.state);
    const { currentModal } = this.state;
    if (!currentModal) return null;

    switch (currentModal) {
      case ModalTypes.PROFILE:
        return <Profile/>;
      case ModalTypes.CROP:
        return <Crop/>;
      default:
        console.warn(`Unsupported modal type: ${currentModal}`);
        return null;
    }
  }
}

export default Container.create(ModalsWrapper);
