/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map, isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import PeerUtils from '../utils/PeerUtils';

import DefaultMessages from './dialog/MessagesSection.react';
import DialogFooter from './dialog/DialogFooter.react';
import DefaultToolbar from './Toolbar.react';
import DefaultActivity from './Activity.react';
import DefaultCall from './Call.react';
import DefaultLogger from './dev/LoggerSection.react';
import ConnectionState from './common/ConnectionState.react';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';

import DialogActionCreators from '../actions/DialogActionCreators';

class DialogSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object
  };

  static propTypes = {
    params: PropTypes.object
  };

  static getStores() {
    return [ActivityStore, DialogStore];
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      isMember: DialogStore.isMember(),
      isActivityOpen: ActivityStore.isOpen()
    };
  }

  constructor(props) {
    super(props);

    const peer = PeerUtils.stringToPeer(props.params.id);
    DialogActionCreators.selectDialogPeer(peer);
  }

  componentWillReceiveProps(nextProps) {
    const { params } = nextProps;
    if (this.props.params.id === params.id) {
      return;
    }

    const peer = PeerUtils.stringToPeer(params.id);
    DialogActionCreators.selectDialogPeer(peer);
  }

  componentWillUnmount() {
    // Unbind from current peer
    DialogActionCreators.selectDialogPeer(null);
  }

  getComponents() {
    const { dialog, logger } = this.context.delegate.components;
    const LoggerSection = logger || DefaultLogger;
    if (dialog && !isFunction(dialog)) {
      const activity = dialog.activity || [
        DefaultActivity,
        DefaultCall,
        LoggerSection
      ];

      return {
        ToolbarSection: dialog.toolbar || DefaultToolbar,
        MessagesSection: isFunction(dialog.messages) ? dialog.messages : DefaultMessages,
        activity: map(activity, (Activity, index) => <Activity key={index} />)
      };
    }

    return {
      ToolbarSection: DefaultToolbar,
      MessagesSection: DefaultMessages,
      activity: [
        <DefaultActivity key={1} />,
        <DefaultCall key={2} />,
        <LoggerSection key={3} />
      ]
    };
  }

  render() {
    const { peer, isMember, messages, overlay, messagesCount } = this.state;

    const {
      ToolbarSection,
      MessagesSection,
      activity
    } = this.getComponents();

    return (
      <section className="main">
        <ToolbarSection />
        <div className="flexrow">
          <section className="dialog">
            <ConnectionState/>
            <MessagesSection peer={peer} isMember={isMember} />
            <DialogFooter isMember={isMember} />
          </section>
          {activity}
        </div>
      </section>
    );
  }
}

export default Container.create(DialogSection);
