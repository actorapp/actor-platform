import React from 'react';
import { PureRenderMixin } from 'react/addons';

import classNames from 'classnames';

import DialogStore from 'stores/DialogStore';

export default React.createClass({
  mixins: [PureRenderMixin],

  getInitialState() {
    return {
      typing: null,
      show: false
    };
  },

  componentDidMount() {
    DialogStore.addTypingListener(this.onTypingChange);
  },

  componentWillUnmount() {
    DialogStore.removeTypingListener(this.onTypingChange);
  },

  onTypingChange() {
    const typing = DialogStore.getSelectedDialogTyping();
    if (typing === null) {
      this.setState({show: false});
    } else {
      this.setState({typing: typing, show: true});
    }
  },

  render() {
    const typing = this.state.typing;
    const show = this.state.show;
    const typingClassName = classNames('typing', {
      'typing--hidden': show === false
    });

    return (
      <div className={typingClassName}>
        <div className="typing-indicator"><i></i><i></i><i></i></div>
        <span>{typing}</span>
      </div>
    );
  }
});
