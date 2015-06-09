import React from 'react';
import { PureRenderMixin } from 'react/addons';

import classNames from 'classnames';

import DialogStore from '../../stores/DialogStore';

export default React.createClass({
  mixins: [PureRenderMixin],

  getInitialState: function() {
    return {typing: null};
  },
  componentDidMount: function() {
    DialogStore.addTypingListener(this._onTypingChange);
  },

  componentWillUnmount: function() {
    DialogStore.removeTypingListener(this._onTypingChange);
  },

  render: function() {
    var typing = this.state.typing;
    var typingClassName = classNames('typing', {
      'typing--hidden': typing == null
    });

    return (
      <div className={typingClassName}>
        <i className="material-icons">keyboard</i>
        <span>{typing}</span>
      </div>
    );
  },

  _onTypingChange: function() {
    var typing = DialogStore.getSelectedDialogTyping();
    this.setState({typing: typing});
  }

});
