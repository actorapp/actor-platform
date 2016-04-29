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
    isMember: PropTypes.bool.isRequired,
    isBlocked: PropTypes.bool.isRequired,
    onUnblock: PropTypes.func.isRequired
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
    const { isMember, isBlocked, onUnblock } = this.props;
    if (!isMember) {
      return (
        <footer className="chat__footer chat__footer--disabled">
          <FormattedMessage id="compose.notMemger" />
        </footer>
      );
    }

    if (isBlocked) {
      return (
        <footer className="chat__footer chat__footer--disabled chat__footer--clickable" onClick={onUnblock}>
          <FormattedMessage id="compose.unblock" />
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
