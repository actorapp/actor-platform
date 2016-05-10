/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { ModalTypes } from '../../constants/ActorAppConstants';
import DelegateContainer from '../../utils/DelegateContainer';

import ModalStore from '../../stores/ModalStore';

import Profile from './Profile.react';
import Crop from './Crop.react';
import Groups from './Groups.react';
import People from './People.react';
import AddContact from './AddContact.react';
import CreateGroup from './CreateGroup.react';
import EditGroup from './EditGroup.react';
import Preferences from './Preferences.react';
import Invite from './Invite.react';
import InviteByLink from './InviteByLink.react';
import QuickSearch from './QuickSearch.react';
import Attachments from './Attachments.react';
import BlockedUsers from './BlockedUsers.react';
import DefaultAbout from './About.react';

class ModalsWrapper extends Component {
  static getStores() {
    return [ModalStore];
  }

  static calculateState() {
    return ModalStore.getState();
  }

  constructor(props) {
    super(props);

    this.components = this.getComponents();
  }

  getComponents() {
    const { components } = DelegateContainer.get();
    const modal = components.modal;

    // TODO: Add more components
    if (modal) {
      return {
        About: isFunction(modal.about) ? modal.about : DefaultAbout
      };
    }

    return {
      About: DefaultAbout
    };
  }

  render() {
    const { currentModal } = this.state;
    if (!currentModal) return null;

    const { About } = this.components;

    switch (currentModal) {
      case ModalTypes.PROFILE:
        return <Profile/>;
      case ModalTypes.CROP:
        return <Crop/>;
      case ModalTypes.GROUP_LIST:
        return <Groups/>;
      case ModalTypes.PEOPLE_LIST:
        return <People/>;
      case ModalTypes.ADD_CONTACT:
        return <AddContact/>;
      case ModalTypes.CREATE_GROUP:
        return <CreateGroup/>;
      case ModalTypes.EDIT_GROUP:
        return <EditGroup/>;
      case ModalTypes.PREFERENCES:
        return <Preferences/>;
      case ModalTypes.INVITE:
        return <Invite/>;
      case ModalTypes.INVITE_BY_LINK:
        return <InviteByLink/>;
      case ModalTypes.QUICK_SEARCH:
        return <QuickSearch/>;
      case ModalTypes.ATTACHMENTS:
        return <Attachments/>;
      case ModalTypes.BLOCKED_USERS:
        return <BlockedUsers/>;
      case ModalTypes.ABOUT:
        return <About/>;

      default:
        console.warn(`Unsupported modal type: ${currentModal}`);
        return null;
    }
  }
}

export default Container.create(ModalsWrapper);
