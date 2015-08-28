import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
const {addons: { PureRenderMixin }} = addons;

import _ from 'lodash';

import VisibilityStore from 'stores/VisibilityStore';

import MessageActionCreators from 'actions/MessageActionCreators';

import MessageItem from 'components/common/MessageItem.react';
import { MessageContentTypes } from 'constants/ActorAppConstants';

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, (p) => {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

let flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

let lastMessageDate = null,
    lastMessageSenderId = null,
    lastMessageContentType = null;

@ReactMixin.decorate(PureRenderMixin)
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
        dateDivider = null;

    const month = [
      'January', 'February', 'March', 'April', 'May', 'June',
      'July', 'August', 'September', 'October', 'November', 'December'
    ];

    if (lastMessageDate === null) {
      lastMessageDate = new Date(message.fullDate);
    }

    const isFirstMessage = index === 0;
    const isNewDay = date.getDate() !== lastMessageDate.getDate();

    if (isNewDay) {
      dateDivider = (
        <li className="date-divider">{month[date.getMonth()]} {date.getDate()}</li>
      );
    }

    let isSameSender = message.sender.peer.id === lastMessageSenderId &&
                      lastMessageContentType !== MessageContentTypes.SERVICE &&
                      message.content.content !== MessageContentTypes.SERVICE &&
                      !isFirstMessage &&
                      !isNewDay;

    const messageItem = (
      <MessageItem index={index}
                   key={message.sortKey}
                   message={message}
                   isNewDay={isNewDay}
                   isSameSender={isSameSender}
                   onVisibilityChange={this.onMessageVisibilityChange}
                   peer={this.props.peer}/>
    );

    lastMessageDate = new Date(message.fullDate);
    lastMessageContentType = message.content.content;
    lastMessageSenderId = message.sender.peer.id;

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
  };

  render() {
    const messages = _.map(this.props.messages, this.getMessagesListItem);

    return (
      <ul className="messages__list">
        {messages}
      </ul>
    );
  }
}

export default MessagesSection;
