/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { debounce, forEach } from 'lodash';

import { PeerTypes } from '../constants/ActorAppConstants';

import PeerUtils from '../utils/PeerUtils';

import MessagesSection from './dialog/MessagesSection.react';
import DefaultTypingSection from './dialog/TypingSection.react';
import DefaultComposeSection from './dialog/ComposeSection.react';
import DefaultToolbarSection from './ToolbarSection.react';
import DefaultActivitySection from './ActivitySection.react';
import ConnectionState from './common/ConnectionState.react';

import ActivityStore from '../stores/ActivityStore';
import DialogStore from '../stores/DialogStore';
import MessageStore from '../stores/MessageStore';
import GroupStore from '../stores/GroupStore';

import DialogActionCreators from '../actions/DialogActionCreators';

// On which scrollTop value start loading older messages
const LoadMessagesScrollTop = 100;

const initialRenderMessagesCount = 20;
const renderMessagesStep = 20;

let renderMessagesCount = initialRenderMessagesCount;

let lastPeer = null;
let lastScrolledFromBottom = 0;

const getStateFromStores = () => {
  const messages = MessageStore.getAll();
  let messagesToRender;

  if (messages.length > renderMessagesCount) {
    messagesToRender = messages.slice(messages.length - renderMessagesCount);
  } else {
    messagesToRender = messages;
  }

  return {
    peer: DialogStore.getSelectedDialogPeer(),
    messages: messages,
    messagesToRender: messagesToRender
  };
};

class DialogSection extends Component {
  static contextTypes = {
    delegate: PropTypes.object
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ActivityStore.addChangeListener(this.fixScrollTimeout);
    DialogStore.addSelectListener(this.onSelectedDialogChange);
    MessageStore.addChangeListener(this.onMessagesChange);
  }

  componentWillUnmount() {
    ActivityStore.removeChangeListener(this.fixScrollTimeout.bind(this));
    DialogStore.removeSelectListener(this.onSelectedDialogChange);
    MessageStore.removeChangeListener(this.onMessagesChange);
  }

  componentDidMount() {
    const peer = DialogStore.getSelectedDialogPeer();

    if (peer) {
      DialogActionCreators.onConversationOpen(peer);
      this.fixScroll();
      this.loadMessagesByScroll();
    }
  }

  componentDidUpdate() {
    this.fixScroll();
    this.loadMessagesByScroll();
  }

  render() {
    const { peer } = this.state;
    const { delegate } = this.context;

    let mainContent,
        activity = [],
        ToolbarSection, TypingSection, ComposeSection;

    if (delegate.components.dialog !== null && typeof delegate.components.dialog !== 'function') {
      ToolbarSection = delegate.components.dialog.toolbar || DefaultToolbarSection;
      TypingSection = delegate.components.dialog.typing || DefaultTypingSection;
      ComposeSection = delegate.components.dialog.compose || DefaultComposeSection;
      if (delegate.components.dialog.activity) {
        forEach(delegate.components.dialog.activity, (Activity) => activity.push(<Activity/>));
      }
    } else {
      ToolbarSection = DefaultToolbarSection;
      TypingSection = DefaultTypingSection;
      ComposeSection = DefaultComposeSection;
      activity.push(<DefaultActivitySection/>);
    }

    if (peer) {
      let isMember = true,
          memberArea;

      if (peer.type === PeerTypes.GROUP) {
        const group = GroupStore.getGroup(peer.id);
        isMember = DialogStore.isGroupMember(group);
      }

      if (isMember) {
        memberArea = (
          <div>
            <TypingSection/>
            <ComposeSection peer={peer}/>
          </div>
        );
      } else {
        memberArea = (
          <section className="compose compose--disabled row center-xs middle-xs">
            <h3>You are not a member</h3>
          </section>
        );
      }

      mainContent = (
        <section className="dialog" onScroll={this.loadMessagesByScroll}>
          <ConnectionState/>

          <div className="messages">
            <MessagesSection messages={this.state.messagesToRender}
                             peer={peer}
                             ref="MessagesSection"/>

          </div>

          {memberArea}
        </section>
      );
    } else {
      mainContent = (
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
    }

    return (
      <section className="main">
        <ToolbarSection/>

        <div className="flexrow">
          {mainContent}
          {activity}
        </div>
      </section>
    );
  }

  fixScrollTimeout = () => {
    setTimeout(this.fixScroll, 50);
  };

  fixScroll = () => {
    const node = React.findDOMNode(this.refs.MessagesSection);
    if (node) {
      node.scrollTop = node.scrollHeight - lastScrolledFromBottom - node.offsetHeight;
    }
  };

  onSelectedDialogChange = () => {
    lastScrolledFromBottom = 0;
    renderMessagesCount = initialRenderMessagesCount;

    // TODO: Move this to actions
    if (lastPeer != null) {
      DialogActionCreators.onConversationClosed(lastPeer);
    }

    lastPeer = DialogStore.getSelectedDialogPeer();
    DialogActionCreators.onConversationOpen(lastPeer);
  };

  onMessagesChange = debounce(() => {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true});

  loadMessagesByScroll = debounce(() => {
    if (this.state.peer) {
      let node = React.findDOMNode(this.refs.MessagesSection);
      let scrollTop = node.scrollTop;
      lastScrolledFromBottom = node.scrollHeight - scrollTop - node.offsetHeight; // was node.scrollHeight - scrollTop

      if (node.scrollTop < LoadMessagesScrollTop) {
        DialogActionCreators.onChatEnd(this.state.peer);

        if (this.state.messages.length > this.state.messagesToRender.length) {
          renderMessagesCount += renderMessagesStep;

          if (renderMessagesCount > this.state.messages.length) {
            renderMessagesCount = this.state.messages.length;
          }

          this.setState(getStateFromStores());
        }
      }
    }
  }, 5, {maxWait: 30});
}

export default DialogSection;
