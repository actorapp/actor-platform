/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';

export default class TextInput extends Component {
  static propTypes = {
    floatingLabel: React.PropTypes.string,
    type: React.PropTypes.string,
    ref: React.PropTypes.string,

    onChange: React.PropTypes.func,
    onFocus: React.PropTypes.func,
    onBlur: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      value: '',
      isFocused: false
    }
  }

  render() {
    const { floatingLabel, type, ref } = this.props;
    const { value, isFocused } = this.state;

    const inputClassName = classnames('input input__material', {
      'input__material--focus': isFocused,
      'input__material--filled': value.length > 0
    });

    const randomId = 'input-' + Math.random().toString(36).substr(2, 5);

    const label = floatingLabel ? <label htmlFor={randomId}>{floatingLabel}</label> : null;

    return (
      <div className={inputClassName}>
        {label}
        <input
          type={type || 'text'}
          id={randomId}
          onChange={this.handleChange}
          onFocus={this.handleFocus}
          onBlur={this.handleBlur}
          value={value}
          ref={ref}
        />
      </div>
    );
  }

  handleChange = (event) => {
    const { onChange } = this.props;
    const { value } = event.target;
    this.setState({value});
    onChange && onChange();
  };

  handleFocus = (event) => {
    const { onFocus } = this.props;
    const isFocused = true;
    this.setState({isFocused});
    onFocus && onFocus();
  };

  handleBlur = (event) => {
    const { onBlur } = this.props;
    const isFocused = false;
    this.setState({isFocused});
    onBlur && onBlur();
  }
}
