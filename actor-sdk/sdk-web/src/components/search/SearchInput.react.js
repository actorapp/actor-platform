/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';
import classnames from 'classnames';

import { KeyCodes } from '../../constants/ActorAppConstants';

class SearchInput extends Component {
  static contextTypes = {
    intl: PropTypes.object.isRequired
  };

  static propTypes = {
    className: PropTypes.string,
    value: PropTypes.string.isRequired,
    // isFocused: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    // onToggleOpen: PropTypes.func.isRequired,
    onToggleFocus: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.handleInputChange = this.handleInputChange.bind(this);
    this.handleInputBlur = this.handleInputBlur.bind(this);
    this.handleInputFocus = this.handleInputFocus.bind(this);
    this.handleClearClick = this.handleClearClick.bind(this);
    // this.onKeyDown = this.onKeyDown.bind(this);
  }

  componentDidMount() {
    findDOMNode(this.refs.search).focus();
    // document.addEventListener('keydown', this.onKeyDown, false);
  }

  // componentWillUnmount() {
  //   document.removeEventListener('keydown', this.onKeyDown, false);
  // }

  handleInputChange(event) {
    this.props.onChange(event.target.value);
  }

  handleInputFocus() {
    // if (this.props.value.length) {
    //   this.props.onToggleOpen(true);
    // }
    this.props.onToggleFocus(true);
  }

  handleInputBlur() {
    // if (!this.props.value.length) {
    //   this.props.onToggleOpen(false);
    // }
    this.props.onToggleFocus(false);
  }

  handleClearClick() {
    this.props.onChange('');
    // this.props.onToggleOpen(false);
    this.props.onToggleFocus(false);
  }

  // onKeyDown(event) {
  //   if (this.props.isOpen && event.keyCode === KeyCodes.ESC) {
  //     event.preventDefault();
  //     this.onClear();
  //   }
  // }

  renderInput() {
    const { value } = this.props;
    const { intl } = this.context;

    return (
      <input
        className="input input--search col-xs"
        type="search"
        ref="search"
        tabIndex="1"
        value={value}
        placeholder={intl.messages['search.placeholder']}
        onChange={this.handleInputChange}
        onFocus={this.handleInputFocus}
        onBlur={this.handleInputBlur}
      />
    );
  }

  renderClear() {
    const { value } = this.props;

    if (!value || !value.length) {
      return null;
    }

    return (
      <i className="close-icon material-icons" onClick={this.handleClearClick}>close</i>
    );
  }

  render() {
    const { className } = this.props;
    const searchClassName = classnames('row', className);

    return (
      <div className={searchClassName}>
        {this.renderInput()}
        {this.renderClear()}
      </div>
    );
  }
}

export default SearchInput;
