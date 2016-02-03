/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { debounce, forEach } from 'lodash';

import { PeerTypes } from '../constants/ActorAppConstants';

import DefaultMessagesSection from './dialog/MessagesSection.react';
import DefaultTypingSection from './dialog/TypingSection.react';
import DefaultComposeSection from './dialog/ComposeSection.react';
import DefaultToolbarSection from './ToolbarSection.react';
import DefaultActivitySection from './ActivitySection.react';
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

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ActivityStore.addListener(this.fixScrollTimeout);
    MessageStore.addListener(this.onMessagesChange);
    DialogStore.addListener(this.onChange);
  }

  componentDidMount() {
    const { peer } = this.state;

    if (peer) {
      this.fixScroll();
      this.loadMessagesByScroll();
    }
  }

  componentDidUpdate() {
    this.fixScroll();
    this.loadMessagesByScroll();
  }

  render() {
    const { peer, isMember, messagesToRender, overlayToRender } = this.state;
    const { delegate } = this.context;

    let activity = [],
        ToolbarSection,
        TypingSection,
        ComposeSection,
        MessagesSection;

    if (delegate.components.dialog !== null && typeof delegate.components.dialog !== 'function') {
      ToolbarSection = delegate.components.dialog.toolbar || DefaultToolbarSection;
      MessagesSection = (typeof delegate.components.dialog.messages == 'function') ? delegate.components.dialog.messages : DefaultMessagesSection;
      TypingSection = delegate.components.dialog.typing || DefaultTypingSection;
      ComposeSection = delegate.components.dialog.compose || DefaultComposeSection;

      if (delegate.components.dialog.activity) {
        forEach(delegate.components.dialog.activity, (Activity) => activity.push(<Activity/>));
      } else {
        activity.push(<DefaultActivitySection/>);
      }
    } else {
      ToolbarSection = DefaultToolbarSection;
      MessagesSection = DefaultMessagesSection;
      TypingSection = DefaultTypingSection;
      ComposeSection = DefaultComposeSection;
      activity.push(<DefaultActivitySection/>);
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

    const emptyScreen = (
      <section className="dialog dialog--empty row center-xs middle-xs">
        <ConnectionState/>

        <div className="advice">
          <div className="actor-logo">
            <svg className="icon icon--gray"
                 dangerouslySetInnerHTML={{__html: '<use xlink:href="assets/images/icons.svg#star"/>'}}/>
          </div>
          <h2>Try to be better than yesterday!</h2>
        </div>
      </section>
    );

    return (
      <section className="main">
        {
          peer
            ? <ToolbarSection/>
            : null
        }
        <div className="flexrow">
          {
            peer
              ? mainScreen
              : emptyScreen
          }
          {activity}
        </div>
      </section>
    );
  }

  fixScrollTimeout = () => {
    setTimeout(this.fixScroll, 50);
  };

  fixScroll = () => {
    const scrollNode = React.findDOMNode(this.refs.messagesSection.refs.messagesScroll.refs.scroll);
    const node = scrollNode.getElementsByClassName('ss-content')[0];
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
      const scrollNode = React.findDOMNode(this.refs.messagesSection.refs.messagesScroll.refs.scroll);
      const node = scrollNode.getElementsByClassName('ss-content')[0];
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
}

export default DialogSection;
