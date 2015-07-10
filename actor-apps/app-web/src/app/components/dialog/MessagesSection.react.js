import React from 'react';

import _ from 'lodash';

import VisibilityStore from '../../stores/VisibilityStore';

import MessageActionCreators from '../../actions/MessageActionCreators';

import MessageItem from '../common/MessageItem.react';

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, (p) => {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

let flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

class MessagesSection extends React.Component {
  static propTypes = {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
  };

  componentWillUnmount() {
    VisibilityStore.removeChangeListener(this.onAppVisibilityChange);
  }

  constructor(props) {
    super(props);

    VisibilityStore.addChangeListener(this.onAppVisibilityChange);
  }

  getMessagesListItem = (message) => {
    return (
      <MessageItem key={message.sortKey} message={message} onVisibilityChange={this.onMessageVisibilityChange} peer={this.props.peer}/>
    );
  }

  onAppVisibilityChange = () => {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  }

  onMessageVisibilityChange = (message, isVisible) => {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  }

  render() {
    let messages = _.map(this.props.messages, this.getMessagesListItem);

    return (
      <ul className="messages">
        {messages}
      </ul>
    );
  }
}

export default MessagesSection;
