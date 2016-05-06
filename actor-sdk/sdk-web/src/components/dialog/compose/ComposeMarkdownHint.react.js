/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

class ComposeMarkdownHint extends Component {
  static contextTypes = {
    intl: PropTypes.object.isRequired
  };

  static propTypes = {
    isActive: PropTypes.bool.isRequired
  };

  shouldComponentUpdate(nextProps) {
    return nextProps.isActive !== this.props.isActive;
  }

  render() {
    const { intl } = this.context;
    const className = classNames('compose__markdown-hint', {
      'compose__markdown-hint--active': this.props.isActive
    });

    return (
      <div className={className}>
        <b>*{intl.messages['compose.markdown.bold']}*</b>
        &nbsp;&nbsp;
        <i>_{intl.messages['compose.markdown.italic']}_</i>
        &nbsp;&nbsp;
        <code>```{intl.messages['compose.markdown.preformatted']}```</code>
      </div>
    );
  }
}

export default ComposeMarkdownHint;
