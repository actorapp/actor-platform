/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { debounce, forEach } from 'lodash';

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import PeerUtils from '../utils/PeerUtils';

import DefaultMessages from './dialog/MessagesSection.react';
import DefaultTyping from './dialog/TypingSection.react';
import DefaultCompose from './dialog/ComposeSection.react';
import DefaultToolbar from './Toolbar.react';
import DefaultActivity from './Activity.react';
import DefaultCall from './Call.react';
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

  fixScroll = () => {
    const scrollNode = findDOMNode(this.refs.messagesSection.refs.messagesScroll.refs.scroll);
    const node = scrollNode.getElementsByClassName('ss-scrollarea')[0];
    if (node) {
      node.scrollTop = node.scrollHeight - lastScrolledFromBottom - node.offsetHeight;
    }
  };

  onChange = () => {
    lastScrolledFromBottom = 0;
    renderMessagesCount = initialRenderMessagesCount;
    this.setState(getStateFromStores());
  };

  onMessagesChange = debounce(() => {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true});

  loadMessagesByScroll = debounce(() => {
    const { peer, messages, messagesToRender } = this.state;

    if (peer) {
      const scrollNode = findDOMNode(this.refs.messagesSection.refs.messagesScroll.refs.scroll);
      const node = scrollNode.getElementsByClassName('ss-scrollarea')[0];
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


  render() {
    const { peer, isMember, messagesToRender, overlayToRender } = this.state;
    const { delegate } = this.context;

    let activity = [],
        ToolbarSection,
        TypingSection,
        ComposeSection,
        MessagesSection;

    if (delegate.components.dialog !== null && typeof delegate.components.dialog !== 'function') {
      ToolbarSection = delegate.components.dialog.toolbar || DefaultToolbar;
      MessagesSection = (typeof delegate.components.dialog.messages == 'function') ? delegate.components.dialog.messages : DefaultMessages;
      TypingSection = delegate.components.dialog.typing || DefaultTyping;
      ComposeSection = delegate.components.dialog.compose || DefaultCompose;

      if (delegate.components.dialog.activity) {
        forEach(delegate.components.dialog.activity, (Activity, index) => activity.push(<Activity key={index}/>));
      } else {
        activity.push(<DefaultActivity key={1}/>);
      }
    } else {
      ToolbarSection = DefaultToolbar;
      MessagesSection = DefaultMessages;
      TypingSection = DefaultTyping;
      ComposeSection = DefaultCompose;
      activity.push(<DefaultActivity key={1}/>);
      activity.push(<DefaultCall key={2} />);
    }

    const mainScreen = peer ? (
      <section className="dialog">
        <ConnectionState/>
        <div className="messages">
          <MessagesSection messages={messagesToRender}
                           overlay={overlayToRender}
                           peer={peer}
                           ref="messagesSection"
                           onScroll={this.loadMessagesByScroll}/>

        </div>
        {
          isMember
            ? <footer className="dialog__footer">
                <TypingSection/>
                <ComposeSection/>
              </footer>
            : <footer className="dialog__footer dialog__footer--disabled row center-xs middle-xs ">
                <h3>You are not a member</h3>
              </footer>
        }
      </section>
    ) : null;

    return (
      <section className="main">
        <ToolbarSection/>
        <div className="flexrow">
          {mainScreen}
          {activity}
        </div>
      </section>
    );
  }
}

export default DialogSection;
