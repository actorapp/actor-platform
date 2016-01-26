/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component } from 'react';
import classnames from 'classnames';

class TextField extends Component {
  static propTypes = {
    className: React.PropTypes.string,
    floatingLabel: React.PropTypes.string,
    type: React.PropTypes.string,
    value: React.PropTypes.string,
    ref: React.PropTypes.string,
    disabled: React.PropTypes.bool,

    onChange: React.PropTypes.func,
    onFocus: React.PropTypes.func,
    onBlur: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isFocused: false,
      inputId: 'input-' + Math.random().toString(36).substr(2, 5)
    };
  }

  render() {
    const { className, floatingLabel, type, value, ref, disabled } = this.props;
    const { isFocused, inputId } = this.state;

    const inputClassName = classnames('input input__material', className, {
      'input__material--focus': isFocused,
      'input__material--filled': value && value.length > 0,
      'input__material--disabled': disabled
    });

    const inputProps = {
      type: type || 'text',
      id: inputId,
      onChange: this.handleChange,
      onFocus: this.handleFocus,
      onBlur: this.handleBlur,
      value,
      disabled,
      ref: ref ? ref : 'input'
    };

    return (
      <div className={inputClassName}>
        {
          floatingLabel
            ? <label htmlFor={inputId} onMouseDown={this.focus}>{floatingLabel}</label>
            : null
        }
        <input {...inputProps}/>
      </div>
    );
  }

  focus = () => {
    const { ref } = this.props;
    setTimeout(() => {
      React.findDOMNode(ref ? ref : this.refs.input).focus();
    }, 0);
  };

  handleChange = (event) => {
    const { onChange } = this.props;
    onChange && onChange(event);
  };

  handleFocus = (event) => {
    const { onFocus } = this.props;
    this.setState({isFocused: true});
    onFocus && onFocus(event);
  };

  handleBlur = (event) => {
    const { onBlur } = this.props;
    this.setState({isFocused: false});
    onBlur && onBlur(event);
  };
}

export default TextField;
