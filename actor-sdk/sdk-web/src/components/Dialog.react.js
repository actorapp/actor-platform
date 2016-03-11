/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { debounce, map, isFunction } from 'lodash';

import React, { Component, PropTypes } from 'react';
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
const initialRenderMessagesCount = 20;
const renderMessagesStep = 20;

let renderMessagesCount = initialRenderMessagesCount;
let lastScrolledFromBottom = 0;

const getStateFromStores = () => {
  const messages = MessageStore.getAll();
  const overlay = MessageStore.getOverlay();
  const messagesToRender = (messages.length > renderMessagesCount) ? messages.slice(messages.length - renderMessagesCount) : messages;
  const overlayToRender = (overlay.length > renderMessagesCount) ? overlay.slice(overlay.length - renderMessagesCount) : overlay;

  return {
    peer: DialogStore.getCurrentPeer(),
    messages,
    overlay,
    messagesToRender,
    overlayToRender,
    isMember: DialogStore.isMember()
  };
};

class DialogSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object
  };

  static propTypes = {
    params: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ActivityStore.addListener(this.fixScrollTimeout);
    MessageStore.addListener(this.onMessagesChange);
    DialogStore.addListener(this.onChange);
  }

  componentWillMount() {
    const peer = PeerUtils.stringToPeer(this.props.params.id);
    DialogActionCreators.selectDialogPeer(peer);
  }

  componentDidMount() {
    const peer = PeerUtils.stringToPeer(this.props.params.id);
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
      if (peer) {
        this.fixScroll();
        this.loadMessagesByScroll();
      }
    }
  }

  componentDidUpdate() {
    this.fixScroll();
  }

  componentWillUnmount() {
    DialogActionCreators.selectDialogPeer(null);
  }

  fixScrollTimeout = () => {
    setTimeout(this.fixScroll, 50);
  };

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

  onChange = () => {
    const nextState = getStateFromStores();
    if (nextState.peer !== this.state.peer || nextState.messages.length !== this.state.messages.length) {
      lastScrolledFromBottom = 0;
      renderMessagesCount = initialRenderMessagesCount;
    }

    this.setState(nextState);
  };

  onMessagesChange = debounce(() => {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true});

  loadMessagesByScroll = debounce(() => {
    const { peer, messages, messagesToRender } = this.state;

    if (peer) {
      const node = this.getScrollArea();
      let scrollTop = node.scrollTop;
      lastScrolledFromBottom = node.scrollHeight - scrollTop - node.offsetHeight; // was node.scrollHeight - scrollTop

      if (node.scrollTop < loadMessagesScrollTop) {

        if (messages.length > messagesToRender.length) {
          renderMessagesCount += renderMessagesStep;

          if (renderMessagesCount > messages.length) {
            renderMessagesCount = messages.length;
          }

          this.setState(getStateFromStores());
        } else {
            DialogActionCreators.onChatEnd(peer);
        }
      }
    }
  }, 5, {maxWait: 30});

  getComponents() {
    const {dialog, logger} = this.context.delegate.components;
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
    const { peer, isMember, messagesToRender, overlayToRender } = this.state;

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
                messages={messagesToRender}
                overlay={overlayToRender}
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

export default DialogSection;
