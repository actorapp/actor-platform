/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { ModalTypes } from '../../constants/ActorAppConstants';

import ModalStore from '../../stores/ModalStore';

import Profile from './Profile.react';
import Crop from './Crop.react';
import GroupList from './GroupList.react';
import AddContact from './AddContact.react';
import CreateGroup from './CreateGroup.react';
import EditGroup from './EditGroup.react';

import Preferences from './Preferences.react';

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
      case ModalTypes.GROUP_LIST:
        return <GroupList/>;
      case ModalTypes.ADD_CONTACT:
        return <AddContact/>;
      case ModalTypes.CREATE_GROUP:
        return <CreateGroup/>;
      case ModalTypes.EDIT_GROUP:
        return <EditGroup/>;
      case ModalTypes.PREFERENCES:
        return <Preferences/>;

      default:
        console.warn(`Unsupported modal type: ${currentModal}`);
        return null;
    }
  }
}

export default Container.create(ModalsWrapper);
