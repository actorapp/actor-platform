import React from 'react';

import _ from 'lodash';

import MessageActionCreators from '../../actions/MessageActionCreators';
import VisibilityStore from '../../stores/VisibilityStore';

import VisibilitySensor from 'react-visibility-sensor';
import MessageItem from '../common/MessageItem.react';

var _delayed = [];

var flushDelayed = function() {
  _.forEach(_delayed, function(p) {
    MessageActionCreators.setMessageShown(p.peer, p.message);
  });

  _delayed = [];
};

var flushDelayedDebounced = _.debounce(flushDelayed, 30, 100);

var ReadableMessage = React.createClass({
  propTypes: {
    peer: React.PropTypes.object.isRequired,
    message: React.PropTypes.object.isRequired
  },

  render: function() {
    return (
      <VisibilitySensor onChange={this._onVisibilityChange}>
        <MessageItem message={this.props.message}/>
      </VisibilitySensor>
    );
  },

  _onVisibilityChange: function(isVisible) {
    if (isVisible) {
      _delayed.push({peer: this.props.peer, message: this.props.message});

      if (VisibilityStore.isVisible) {
        flushDelayedDebounced();
      }
    }
  }
});

export default React.createClass({
  propTypes: {
    messages: React.PropTypes.array.isRequired,
    peer: React.PropTypes.object.isRequired
  },

  componentDidMount: function() {
    VisibilityStore.addChangeListener(this._onAppVisibilityChange);
  },

  componentWillUnmount: function() {
    VisibilityStore.removeChangeListener(this._onAppVisibilityChange);
  },

  render: function() {
    var messages = _.map(this.props.messages, this._getMessagesListItem);

    return (
      <ul className="messages">
        {messages}
      </ul>
    );
  },

  _getMessagesListItem: function (message) {
    return (
      <ReadableMessage key={message.sortKey} peer={this.props.peer} message={message}/>
    );
  },

  _onAppVisibilityChange: function() {
    if (VisibilityStore.isVisible) {
      flushDelayed();
    }
  }
});

