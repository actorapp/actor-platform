/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import classnames from 'classnames';
import { KeyCodes } from '../../constants/ActorAppConstants';

class SearchInput extends Component {
  static contextTypes = {
    intl: PropTypes.object.isRequired
  };

  static propTypes = {
    className: PropTypes.string,
    value: PropTypes.string.isRequired,
    onClear: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired,
    onToggleFocus: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleBlur = this.handleBlur.bind(this);
    this.handleFocus = this.handleFocus.bind(this);
    this.handleClear = this.handleClear.bind(this);
  }

  componentDidMount() {
    this.listeners = [
      EventListener.listen(document, 'keydown', this.handleKeyDown)
    ];
  }

  componentWillUnmount() {
    this.listeners.forEach((listener) => listener.remove());
    this.listeners = null;
  }

  handleBlur() {
    this.props.onToggleFocus(false);
  }

  handleFocus() {
    this.props.onToggleFocus(true);
  }

  handleChange(event) {
    this.props.onChange(event.target.value);
  }

  handleClear() {
    this.props.onClear();
    this.props.onChange('');
    this.props.onToggleFocus(false);
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.K && (event.metaKey || event.ctrlKey)) {
      event.preventDefault();
      this.focus();
    }

    if (event.keyCode === KeyCodes.ESC && this.isFocused()) {
      event.preventDefault();
      this.handleClear();
    }
  }

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
        onBlur={this.handleBlur}
        onFocus={this.handleFocus}
        onChange={this.handleChange}
      />
    );
  }

  renderClear() {
    const { value } = this.props;

    if (!value || !value.length) {
      return null;
    }

    return (
      <i className="close-icon material-icons" onClick={this.handleClear}>close</i>
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

  focus() {
    if (this.refs.search) {
      this.refs.search.focus();
    }
  }

  isFocused() {
    return document.activeElement === this.refs.search;
  }
}

export default SearchInput;
