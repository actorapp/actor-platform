/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';

import DefaultHeaderSection from './sidebar/HeaderSection.react';
import DefaultRecent from './sidebar/Recent.react';
import QuickSearchButton from './sidebar/QuickSearchButton.react';

import DialogStore from '../stores/DialogStore';
import ArchiveStore from '../stores/ArchiveStore';

class SidebarSection extends Component {
  static getStores() {
    return [DialogStore, ArchiveStore];
  }

  static calculateState() {
    return {
      currentPeer: DialogStore.getCurrentPeer(),
      dialogs: DialogStore.getDialogs(),
      archive: ArchiveStore.getArchiveChatState()
    };
  }

  static contextTypes = {
    delegate: PropTypes.object
  };

  render() {
    const { delegate } = this.context;
    const { currentPeer, dialogs, archive } = this.state;

    let HeaderSection, Recent, FooterSection;
    if (delegate.components.sidebar !== null && typeof delegate.components.sidebar !== 'function') {
      HeaderSection = delegate.components.sidebar.header || DefaultHeaderSection;
      Recent = delegate.components.sidebar.recent || DefaultRecent;
      FooterSection = delegate.components.sidebar.footer || QuickSearchButton;
    } else {
      HeaderSection = DefaultHeaderSection;
      Recent = DefaultRecent;
      FooterSection = QuickSearchButton;
    }

    return (
      <aside className="sidebar">
        <HeaderSection/>
        <Recent currentPeer={currentPeer} dialogs={dialogs} archive={archive} />
        <FooterSection/>
      </aside>
    );
  }
}

export default Container.create(SidebarSection, { pure: false });
