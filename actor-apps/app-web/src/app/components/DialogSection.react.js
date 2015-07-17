import _ from 'lodash';

import React from 'react';

import { PeerTypes } from 'constants/ActorAppConstants';

import MessagesSection from 'components/dialog/MessagesSection.react';
import TypingSection from 'components/dialog/TypingSection.react';
import ComposeSection from 'components/dialog/ComposeSection.react';

import DialogStore from 'stores/DialogStore';
import MessageStore from 'stores/MessageStore';
import GroupStore from 'stores/GroupStore';

import DialogActionCreators from 'actions/DialogActionCreators';

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

class DialogSection extends React.Component {
  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    DialogStore.addSelectListener(this.onSelectedDialogChange);
    MessageStore.addChangeListener(this.onMessagesChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectListener(this.onSelectedDialogChange);
    MessageStore.removeChangeListener(this.onMessagesChange);
  }

  componentDidUpdate() {
    this.fixScroll();
    this.loadMessagesByScroll();
  }

  render() {
    const peer = this.state.peer;

    if (peer) {
      let isMember = true;
      let memberArea;
      if (peer.type === PeerTypes.GROUP) {
        const group = GroupStore.getGroup(peer.id);
        isMember = DialogStore.isGroupMember(group);
      }

      if (isMember) {
        memberArea = (
          <div>
            <TypingSection/>
            <ComposeSection peer={this.state.peer}/>
          </div>
        );
      } else {
        memberArea = (
          <section className="compose compose--disabled row center-xs middle-xs">
            <h3>You are not member</h3>
          </section>
        );
      }

      return (
        <section className="dialog" onScroll={this.loadMessagesByScroll}>
          <MessagesSection messages={this.state.messagesToRender}
                           peer={this.state.peer}
                           ref="MessagesSection"/>

          {memberArea}
        </section>
      );
    } else {
      return (
        <section className="dialog dialog--empty row middle-xs center-xs" ref="MessagesSection">
          Select dialog or start a new one.
        </section>
      );
    }
  }

  fixScroll = () => {
    let node = React.findDOMNode(this.refs.MessagesSection);
    if (!node.className.includes('dialog--empty')) {
      node.scrollTop = node.scrollHeight - lastScrolledFromBottom;
    }
  }

  onSelectedDialogChange = () => {
    renderMessagesCount = initialRenderMessagesCount;

    if (lastPeer != null) {
      DialogActionCreators.onConversationClosed(lastPeer);
    }

    lastPeer = DialogStore.getSelectedDialogPeer();
    DialogActionCreators.onConversationOpen(lastPeer);
  }

  onMessagesChange = _.debounce(() => {
    this.setState(getStateFromStores());
  }, 10, {maxWait: 50, leading: true});

  loadMessagesByScroll = _.debounce(() => {
    let node = React.findDOMNode(this.refs.MessagesSection);
    if (!node.className.includes('dialog--empty')) {
      let scrollTop = node.scrollTop;
      lastScrolledFromBottom = node.scrollHeight - scrollTop;

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
