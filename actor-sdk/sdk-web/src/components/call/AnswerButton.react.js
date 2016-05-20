/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';
import classNames from 'classnames';

class AnswerButton extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired,
    small: PropTypes.bool
  };

  render() {
    const className = classNames('button', {
      'button--rised button--wide': !this.props.small,
      'button--square col-xs': this.props.small
    });

    return (
      <button className={className} onClick={this.props.onClick}>
        <i className="material-icons" key="icon">call</i>
        <FormattedMessage id="call.answer"/>
      </button>
    );
  }
}

export default AnswerButton;
