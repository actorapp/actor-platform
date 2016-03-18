/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { map, isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import PeerUtils from '../utils/PeerUtils';

import DefaultMessages from './dialog/MessagesSection.react';
import DefaultTyping from './dialog/TypingSection.react';
import DefaultCompose from './dialog/ComposeSection.react';
import DialogFooter from './dialog/DialogFooter.react';
import DefaultToolbar from './Toolbar.react';
import DefaultActivity from './Activity.react';
import DefaultCall from './Call.react';
import DefaultLogger from './dev/LoggerSection.react';
import ConnectionState from './common/ConnectionState.react';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import MessageStore from '../stores/MessageStore';

import DialogActionCreators from '../actions/DialogActionCreators';

class DialogSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object
  };

  static propTypes = {
    params: PropTypes.object
  };

  static getStores() {
    return [ActivityStore, MessageStore, DialogStore]
  }

  static calculateState() {
    return {
      peer: DialogStore.getCurrentPeer(),
      isMember: DialogStore.isMember(),
      messages: MessageStore.getMessages(),
      overlay: MessageStore.getOverlay(),
      messagesCount: MessageStore.getRenderMessagesCount(),
      isActivityOpen: ActivityStore.isOpen()
    };
  }

  constructor(props) {
    super(props);

    const peer = PeerUtils.stringToPeer(props.params.id);
    DialogActionCreators.selectDialogPeer(peer);

    this.onLoadMoreMessages = this.onLoadMoreMessages.bind(this);
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

  onLoadMoreMessages() {
    const { peer } = this.state;
    if (peer) {
      DialogActionCreators.loadMoreMessages(peer);
    }
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
        LoggerSection,
        ToolbarSection: dialog.toolbar || DefaultToolbar,
        MessagesSection: isFunction(dialog.messages) ? dialog.messages : DefaultMessages,
        TypingSection: dialog.typing || DefaultTyping,
        ComposeSection: dialog.compose || DefaultCompose,
        activity: map(activity, (Activity, index) => <Activity key={index} />)
      };
    }

    return {
      LoggerSection,
      ToolbarSection: DefaultToolbar,
      MessagesSection: DefaultMessages,
      TypingSection: DefaultTyping,
      ComposeSection: DefaultCompose,
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
      TypingSection,
      ComposeSection,
      activity
    } = this.getComponents();

    return (
      <section className="main">
        <ToolbarSection />
        <div className="flexrow">
          <section className="dialog">
            <ConnectionState/>
            <MessagesSection
              peer={peer}
              messages={messages}
              overlay={overlay}
              count={messagesCount}
              isMember={isMember}
              onLoadMore={this.onLoadMoreMessages}
            />
            <DialogFooter isMember={isMember} components={{TypingSection, ComposeSection}} />
          </section>
          {activity}
        </div>
      </section>
    );
  }
}

export default Container.create(DialogSection);
