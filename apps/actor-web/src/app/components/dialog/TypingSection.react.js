import React from 'react';
import { PureRenderMixin } from 'react/addons';

import classNames from 'classnames';

import DialogStore from '../../stores/DialogStore';

export default React.createClass({
  mixins: [PureRenderMixin],

  getInitialState() {
    return {typing: null};
  },
  componentDidMount() {
    DialogStore.addTypingListener(this._onTypingChange);
  },

  componentWillUnmount() {
    DialogStore.removeTypingListener(this._onTypingChange);
  },

  _onTypingChange() {
    var typing = DialogStore.getSelectedDialogTyping();
    this.setState({typing: typing});
  },

  render() {
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
  }
});
