/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { getCaretPosition, setCaretToEnd, getClipboardImages } from '../../../utils/ComposeUtils';
import { KeyCodes } from '../../../constants/ActorAppConstants';
import ComposeMarkdownHint from './ComposeMarkdownHint.react';

class ComposeTextArea extends Component {
  static propTypes = {
    value: PropTypes.string.isRequired,
    autoFocus: PropTypes.bool.isRequired,
    sendByEnter: PropTypes.bool.isRequired,
    sendEnabled: PropTypes.bool.isRequired,
    onSubmit: PropTypes.func.isRequired,
    onTyping: PropTypes.func.isRequired,
    onAttachments: PropTypes.func,
    onKeyDown: PropTypes.func.isRequired
  };

  static defaultProps = {
    sendEnabled: true
  };

  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.onPaste = this.onPaste.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onWindowFocus = this.onWindowFocus.bind(this);
    this.onDocumentKeyDown = this.onDocumentKeyDown.bind(this);

    this.blur = this.blur.bind(this);
    this.focus = this.focus.bind(this);
    this.autoFocus = this.autoFocus.bind(this);
    this.setCaretToEnd = this.setCaretToEnd.bind(this);
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
    this.props.onTyping(event.target.innerHTML, this.getCaretPosition());
  }

  onPaste(event) {
    // Remove HTML from pasted text
    const text = event.clipboardData.getData('text/plain');
    if (text) {
      event.preventDefault();
      const html = text.replace(/\n/g, '<br>');
      document.execCommand('insertHTML', false, html);
      return;
    }

    // Lookup pasted images
    if (this.props.onAttachments) {
      getClipboardImages(event, (attachments) => {
        if (attachments.length) {
          event.preventDefault();
          this.props.onAttachments(attachments);
        }
      });
    }
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
    return getCaretPosition(this.refs.area);
  }

  render() {
    const { value } = this.props;

    return (
      <div>
        <ComposeMarkdownHint isActive={value.length >= 3} />
        <div
          ref="area"
          contentEditable
          className="compose__message"
          onInput={this.onChange}
          onBlur={this.onChange}
          onPaste={this.onPaste}
          onKeyDown={this.onKeyDown}
          dangerouslySetInnerHTML={{ __html: value }}
        />
      </div>
    );
  }

  focus() {
    if (this.refs.area !== document.activeElement) {
      this.refs.area.focus();
    }
  }

  autoFocus() {
    if (this.props.autoFocus) {
      this.focus();
    }
  }

  setCaretToEnd() {
    setCaretToEnd(this.refs.area);
  }

  blur() {
    this.refs.area.blur();
  }
}

export default ComposeTextArea;
