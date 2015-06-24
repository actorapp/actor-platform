import _ from 'lodash';

import React from 'react';

import MessagesSection from './dialog/MessagesSection.react';
import TypingSection from './dialog/TypingSection.react';
import ComposeSection from './dialog/ComposeSection.react';

import DialogStore from '../stores/DialogStore';
import MessageStore from '../stores/MessageStore';

import DialogActionCreators from '../actions/DialogActionCreators';
//import DraftActions from '../actions/DraftActions';
import DraftActionCreators from '../actions/DraftActionCreators';

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
  componentWillMount() {
    DialogStore.addSelectListener(this.onSelectedDialogChange);
    MessageStore.addChangeListener(this.onMessagesChange);
  }

  componentWillUnmount() {
    DialogStore.removeSelectListener(this.onSelectedDialogChange);
    MessageStore.removeChangeListener(this.onMessagesChange);
  }

  componentDidUpdate() {
    DraftActionCreators.loadDraft(this.state.peer);
    this.fixScroll();
    this.loadMessagesByScroll();
  }

  constructor() {
    super();

    this.fixScroll = this.fixScroll.bind(this);
    this.onSelectedDialogChange = this.onSelectedDialogChange.bind(this);
    this.onMessagesChange = this.onMessagesChange.bind(this);
    this.loadMessagesByScroll = this.loadMessagesByScroll.bind(this);

    this.state = getStateFromStores();
  }

  render() {
    if (this.state.peer) {
      return (
        <section className="dialog" onScroll={this.loadMessagesByScroll}>
          <MessagesSection messages={this.state.messagesToRender}
                           peer={this.state.peer}
                           ref="MessagesSection"/>
          <TypingSection/>
          <ComposeSection peer={this.state.peer}/>
        </section>
      );
    } else {
      return (
        <section className="dialog row middle-xs center-xs">
          Select dialog or start a new one.
        </section>
      );
    }
  }

  fixScroll() {
    const node = React.findDOMNode(this.refs.MessagesSection);
    node.scrollTop = node.scrollHeight - lastScrolledFromBottom;
  }

  onSelectedDialogChange() {
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

    var scrollTop = node.scrollTop;
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
  }, 5, {maxWait: 30})
}

export default DialogSection;
