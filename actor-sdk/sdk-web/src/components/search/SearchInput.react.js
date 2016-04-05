/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

import { KeyCodes } from '../../constants/ActorAppConstants';

class SearchInput extends Component {
  static contextTypes = {
    intl: PropTypes.object.isRequired
  };

  static propTypes = {
    className: PropTypes.string,
    value: PropTypes.string.isRequired,
    isOpen: PropTypes.bool.isRequired,
    isFocused: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    onToggleOpen: PropTypes.func.isRequired,
    onToggleFocus: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.onFocus = this.onFocus.bind(this);
    this.onBlur = this.onBlur.bind(this);
    this.onKeyDown = this.onKeyDown.bind(this);
    this.onClear = this.onClear.bind(this);
  }

  componentDidMount() {
    document.addEventListener('keydown', this.onKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.onKeyDown, false);
  }

  onChange(event) {
    this.props.onChange(event.target.value);
  }

  onFocus() {
    if (this.props.value.length) {
      this.props.onToggleOpen(true);
    }

    this.props.onToggleFocus(true);
  }

  onBlur() {
    if (!this.props.value.length) {
      this.props.onToggleOpen(false);
    }

    this.props.onToggleFocus(false);
  }

  onClear() {
    this.props.onChange('');
    this.props.onToggleOpen(false);
    this.props.onToggleFocus(false);
  }

  onKeyDown(event) {
    if (this.props.isOpen && event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.onClear();
    }
  }

  renderClose() {
    if (!this.props.value.length) {
      return null;
    }

    return (
      <i className="close-icon material-icons" onClick={this.onClear}>close</i>
    );
  }

  render() {
    const { className, value, isFocused } = this.props;
    const { intl } = this.context;

    const searchClassName = classnames('toolbar__search', className, {
      'toolbar__search--focused': isFocused || value.length
    });

    return (
      <div className={searchClassName}>
        <input
          className="input input--search"
          type="search"
          tabIndex="1"
          value={value}
          placeholder={intl.messages['search.placeholder']}
          onFocus={this.onFocus}
          onBlur={this.onBlur}
          onChange={this.onChange}
        />
        <i className="search-icon material-icons">search</i>
        {this.renderClose()}
      </div>
    );
  }
}

export default SearchInput;
