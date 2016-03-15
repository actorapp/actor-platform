/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { debounce, map, isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { Container } from 'flux/utils';
import { findDOMNode } from 'react-dom';
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

// On which scrollTop value start loading older messages
const loadMessagesScrollTop = 100;

let lastScrolledFromBottom = 0;

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
      messages: MessageStore.getMessagesToRender(),
      overlay: MessageStore.getOverlayToRender(),
      isActivityOpen: ActivityStore.isOpen()
    };
  }

  constructor(props) {
    super(props);

    const peer = PeerUtils.stringToPeer(props.params.id);
    DialogActionCreators.selectDialogPeer(peer);
  }

  componentDidMount() {
    const { peer } = this.state;
    if (peer) {
      this.fixScroll();
      this.loadMessagesByScroll();
    }
  }

  componentWillReceiveProps(nextProps) {
    const { params } = nextProps;
    if (this.props.params.id !== params.id) {
      const peer = PeerUtils.stringToPeer(params.id);
      DialogActionCreators.selectDialogPeer(peer);
      lastScrolledFromBottom = 0;
      if (peer) {
        this.fixScroll();
        this.loadMessagesByScroll();
      }
    }
  }

  componentDidUpdate(prevProps, prevState) {
    if (prevState.isActivityOpen !== this.state.isActivityOpen) {
      this.fixScrollTimeout();
    } else {
      this.fixScroll();
    }
  }

  componentWillUnmount() {
    // Unbind from current peer
    DialogActionCreators.selectDialogPeer(null);
  }

  getScrollArea() {
    const scrollNode = findDOMNode(this.refs.messagesSection.refs.messagesScroll.refs.scroll);
    return scrollNode.getElementsByClassName('ss-scrollarea')[0];
  }

  fixScroll = () => {
    const node = this.getScrollArea();
    if (node) {
      node.scrollTop = node.scrollHeight - lastScrolledFromBottom - node.offsetHeight;
    }
  };

  fixScrollTimeout = () => {
    setTimeout(this.fixScroll, 50);
  };

  loadMessagesByScroll = debounce(() => {
    const { peer } = this.state;

    if (peer) {
      const node = this.getScrollArea();
      lastScrolledFromBottom = node.scrollHeight - node.scrollTop - node.offsetHeight;

      if (node.scrollTop < loadMessagesScrollTop) DialogActionCreators.loadMoreMessages(peer);
    }
  }, 5, {maxWait: 30});

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
    const { peer, isMember, messages, overlay } = this.state;

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
            <div className="messages">
              <MessagesSection
                isMember={isMember}
                messages={messages}
                overlay={overlay}
                peer={peer}
                ref="messagesSection"
                onScroll={this.loadMessagesByScroll}
              />
            </div>
            <DialogFooter isMember={isMember} components={{TypingSection, ComposeSection}} />
          </section>
          {activity}
        </div>
      </section>
    );
  }
}

export default Container.create(DialogSection);
