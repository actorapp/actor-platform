import React from 'react';

import _ from 'lodash';

import VisibilityStore from 'stores/VisibilityStore';

import MessageActionCreators from 'actions/MessageActionCreators';

import MessageItem from 'components/common/MessageItem.react';

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, (p) => {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

let flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

let lastMessageDate;

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

  getMessagesListItem = (message, index) => {
    let date = new Date(message.fullDate),
        dateDivider;

    const month = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    if (typeof lastMessageDate === 'undefined') {
      lastMessageDate = new Date(message.fullDate);
    }

    const isNewDay = date.getDate() !== lastMessageDate.getDate();

    if (isNewDay) {
      dateDivider = (
        <li className="date-divider">{month[date.getMonth()]} {date.getDate()}</li>
      );
    }

    const messageItem = (
      <MessageItem index={index}
                   key={message.sortKey}
                   message={message}
                   newDay={isNewDay}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

    lastMessageDate = new Date(message.fullDate);

    return [dateDivider, messageItem];
  };

  onAppVisibilityChange = () => {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  };

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
