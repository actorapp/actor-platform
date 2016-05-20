/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';

import React, { Component } from 'react';
import { Container } from 'flux/utils';
import { ModalTypes } from '../../constants/ActorAppConstants';
import DelegateContainer from '../../utils/DelegateContainer';

import ModalStore from '../../stores/ModalStore';

import DefaultProfile from './Profile.react';
import DefaultCrop from './Crop.react';
import DefaultGroups from './Groups.react';
import DefaultPeople from './People.react';
import DefaultAddContact from './AddContact.react';
import DefaultCreateGroup from './CreateGroup.react';
import DefaultEditGroup from './EditGroup.react';
import DefaultPreferences from './Preferences.react';
import DefaultInvite from './Invite.react';
import DefaultInviteByLink from './InviteByLink.react';
import DefaultQuickSearch from './QuickSearch.react';
import DefaultAttachments from './Attachments.react';

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
    const modals = components.modals;

    if (modals) {
      return {
        Profile: isFunction(modals.profile) ? modals.profile : DefaultProfile,
        Crop: isFunction(modals.crop) ? modals.crop : DefaultCrop,
        Groups: isFunction(modals.groups) ? modals.groups : DefaultGroups,
        People: isFunction(modals.people) ? modals.people : DefaultPeople,
        AddContact: isFunction(modals.addContact) ? modals.addContact : DefaultAddContact,
        CreateGroup: isFunction(modals.createGroup) ? modals.createGroup : DefaultCreateGroup,
        EditGroup: isFunction(modals.editGroup) ? modals.editGroup : DefaultEditGroup,
        Preferences: isFunction(modals.preferences) ? modals.preferences : DefaultPreferences,
        Invite: isFunction(modals.invite) ? modals.invite : DefaultInvite,
        InviteByLink: isFunction(modals.inviteByLink) ? modals.inviteByLink : DefaultInviteByLink,
        QuickSearch: isFunction(modals.quickSearch) ? modals.quickSearch : DefaultQuickSearch,
        Attachments: isFunction(modals.attachments) ? modals.attachments : DefaultAttachments
      };
    }

    return {
      Profile: DefaultProfile,
      Crop: DefaultCrop,
      Groups: DefaultGroups,
      People: DefaultPeople,
      AddContact: DefaultAddContact,
      CreateGroup: DefaultCreateGroup,
      EditGroup: DefaultEditGroup,
      Preferences: DefaultPreferences,
      Invite: DefaultInvite,
      InviteByLink: DefaultInviteByLink,
      QuickSearch: DefaultQuickSearch,
      Attachments : DefaultAttachments
    };
  }

  render() {
    const { currentModal } = this.state;
    if (!currentModal) return null;

    const {
      Profile,
      Crop,
      Groups,
      People,
      AddContact,
      CreateGroup,
      EditGroup,
      Preferences,
      Invite,
      InviteByLink,
      QuickSearch,
      Attachments
    } = this.components;

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

      default:
        console.warn(`Unsupported modal type: ${currentModal}`);
        return null;
    }
  }
}

export default Container.create(ModalsWrapper);
