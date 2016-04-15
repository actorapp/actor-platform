/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import { isFunction } from 'lodash';
import React, { Component, PropTypes } from 'react';

import DefaultTyping from './TypingSection.react';
import DefaultCompose from './ComposeSection.react';

class DialogFooter extends Component {
  static contextTypes = {
    delegate: PropTypes.object.isRequired
  };

  static propTypes = {
    isMember: PropTypes.bool.isRequired
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
    if (!this.props.isMember) {
      return (
        <footer className="chat__footer chat__footer--disabled row center-xs middle-xs">
          <h3>You are not a member</h3>
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
