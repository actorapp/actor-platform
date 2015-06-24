import React from 'react';

import _ from 'lodash';

import VisibilityStore from '../../stores/VisibilityStore';

import MessageActionCreators from '../../actions/MessageActionCreators';

import MessageItem from '../common/MessageItem.react';

let _delayed = [];

let flushDelayed = () => {
  _.forEach(_delayed, function(p) {
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

  componentDidMount() {
    VisibilityStore.addChangeListener(this._onAppVisibilityChange);
  }

  componentWillUnmount() {
    VisibilityStore.removeChangeListener(this._onAppVisibilityChange);
  }

  constructor() {
    super();

    this._getMessagesListItem = this._getMessagesListItem.bind(this);
    this._onAppVisibilityChange = this._onAppVisibilityChange.bind(this);
    this._onMessageVisibilityChange = this._onMessageVisibilityChange.bind(this);
  }

  _getMessagesListItem(message) {
    return (
      <MessageItem key={message.sortKey} message={message} onVisibilityChange={this._onMessageVisibilityChange} peer={this.props.peer}/>
    );
  }

  _onAppVisibilityChange() {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  }

  _onMessageVisibilityChange(message, isVisible) {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  }

  render() {
    let messages = _.map(this.props.messages, this._getMessagesListItem);

    return (
      <ul className="messages">
        {messages}
      </ul>
    );
  }
}

export default MessagesSection;
