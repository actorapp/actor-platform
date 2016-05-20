/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';
import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

import DefaultTyping from './TypingSection.react';
import DefaultCompose from './ComposeSection.react';

class DialogFooter extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    info: PropTypes.object.isRequired,
    isMember: PropTypes.bool.isRequired,
    onUnblock: PropTypes.func.isRequired,
    onStart: PropTypes.func.isRequired
  };

  static defaultProps = {
    isBlocked: false
  };

  constructor(props, context) {
    super(props, context);

    const { dialog } = context.delegate.components;
    if (dialog && !isFunction(dialog)) {
      this.components = {
        TypingSection: dialog.typing || DefaultTyping,
        ComposeSection: dialog.compose || DefaultCompose
      };
    } else {
      this.components = {
        TypingSection: DefaultTyping,
        ComposeSection: DefaultCompose
      };
    }
  }

  render() {
    const { info, isMember, onUnblock, onStart } = this.props;
    if (!isMember) {
      return (
        <footer className="chat__footer chat__footer--disabled">
          <FormattedMessage id="compose.notMember" />
        </footer>
      );
    }

    if (info.isBlocked) {
      return (
        <footer className="chat__footer chat__footer--disabled">
          <button className="button button--flat" onClick={onUnblock}>
            <FormattedMessage id="compose.unblock" />
          </button>
        </footer>
      );
    }

    if (info.isBot && !info.isStarted) {
      return (
        <footer className="chat__footer chat__footer--disabled">
          <button className="button button--flat" onClick={onStart}>
            <FormattedMessage id="compose.start" />
          </button>
        </footer>
      );
    }

    const { TypingSection, ComposeSection } = this.components;

    return (
      <footer className="chat__footer">
        <TypingSection />
        <ComposeSection />
      </footer>
    );
  }
}

export default DialogFooter;
