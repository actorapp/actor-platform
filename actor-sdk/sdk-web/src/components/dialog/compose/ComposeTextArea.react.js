/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import Inputs from '../../../utils/Inputs';
import { KeyCodes } from '../../../constants/ActorAppConstants';

class ComposeTextArea extends Component {
  static propTypes = {
    value: PropTypes.string.isRequired,
    autoFocus: PropTypes.bool.isRequired,
    sendByEnter: PropTypes.bool.isRequired,
    sendEnabled: PropTypes.bool.isRequired,
    onSubmit: PropTypes.func.isRequired,
    onTyping: PropTypes.func.isRequired,
    onPaste: PropTypes.func.isRequired,
    onKeyDown: PropTypes.func.isRequired,

    placholder: PropTypes.string
  };

  static defaultProps = {
    sendEnabled: true,
    onPaste: () => {}
  };

  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onWindowFocus = this.onWindowFocus.bind(this);
    this.onDocumentKeyDown = this.onDocumentKeyDown.bind(this);

    this.blur = this.blur.bind(this);
    this.focus = this.focus.bind(this);
    this.autoFocus = this.autoFocus.bind(this);
  }

  componentDidMount() {
    if (this.props.autoFocus) {
      this.focus();
    }

    window.addEventListener('focus', this.onWindowFocus);
    document.addEventListener('keydown', this.onDocumentKeyDown, false);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.value !== this.props.value ||
           nextProps.autoFocus !== this.props.autoFocus ||
           nextProps.sendEnabled !== this.props.sendEnabled ||
           nextProps.sendByEnter !== this.props.sendByEnter;
  }

  componentWillUnmount() {
    window.removeEventListener('focus', this.onWindowFocus);
    document.removeEventListener('keydown', this.onDocumentKeyDown, false);
  }

  onWindowFocus() {
    this.autoFocus();
  }

  onDocumentKeyDown(event) {
    if (event.target === this.refs.area) {
      // event will be handled by onKeyDown
      return;
    }

    if (!event.metaKey && !event.altKey && !event.ctrlKey && !event.shiftKey) {
      this.autoFocus();
      this.onKeyDown(event);
    }
  }

  onChange(event) {
    this.props.onTyping(event.target.value, this.getCaretPosition());
  }

  onKeyDown(event) {
    if (this.props.sendEnabled && this.isSendEvent(event)) {
      event.preventDefault();
      this.props.onSubmit();
    } else if (this.props.onKeyDown) {
      this.props.onKeyDown(event);
    }
  }

  isSendEvent(event) {
    if (event.keyCode !== KeyCodes.ENTER) {
      return false;
    }

    return this.props.sendByEnter ? !event.shiftKey : event.metaKey;
  }

  getCaretPosition() {
    const { start } = Inputs.getInputSelection(this.refs.area);
    return start;
  }

  render() {
    const { value, placeholder } = this.props;

    return (
      <textarea
        ref="area"
        className="compose__message"
        value={value}
        placeholder={placeholder}
        onChange={this.onChange}
        onKeyDown={this.onKeyDown}
        onPaste={this.props.onPaste}
      />
    );
  }

  focus(force = false) {
    const { area } = this.refs;
    if (force || area !== document.activeElement) {
      area.focus();
      if (typeof area.selectionStart == 'number') {
        area.selectionStart = area.selectionEnd = area.value.length;
      } else if (typeof area.createTextRange != 'undefined') {
        const range = area.createTextRange();
        range.collapse(false);
        range.select();
      }
    }
  }

  autoFocus() {
    if (this.props.autoFocus) {
      this.focus();
    }
  }

  blur() {
    this.refs.area.blur();
  }
}

export default ComposeTextArea;
