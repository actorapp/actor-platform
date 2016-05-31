/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import EventListener from 'fbjs/lib/EventListener';
import { KeyCodes } from '../../constants/ActorAppConstants';

class SearchInput extends Component {
  static contextTypes = {
    intl: PropTypes.object.isRequired
  };

  static propTypes = {
    value: PropTypes.string.isRequired,
    isFocused: PropTypes.bool.isRequired,
    onBlur: PropTypes.func.isRequired,
    onFocus: PropTypes.func.isRequired,
    onClear: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.handleFocus = this.handleFocus.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleGlobalClick = this.handleGlobalClick.bind(this);
  }

  componentDidMount() {
    this.listeners = [
      EventListener.listen(document, 'keydown', this.handleKeyDown),
      EventListener.listen(document, 'click', this.handleGlobalClick)
    ];
  }

  componentWillUnmount() {
    this.listeners.forEach((listener) => listener.remove());
    this.listeners = null;
  }

  handleGlobalClick(event) {
   if (this.props.isFocused && event.target !== this.refs.search) {
     this.props.onBlur();
   }
 }

  handleFocus(event) {
    event.preventDefault();
    event.stopPropagation();

    this.props.onFocus();
  }

  handleChange(event) {
    this.props.onChange(event.target.value);
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.K && (event.metaKey || event.ctrlKey)) {
      event.preventDefault();
      this.focus();
    }

    if (event.keyCode === KeyCodes.ESC && this.isFocused()) {
      event.preventDefault();
      this.props.onClear();
    }
  }

  renderClear() {
    if (!this.props.value) {
      return null;
    }

    return (
      <i className="close-icon material-icons" onClick={this.props.onClear}>close</i>
    );
  }

  render() {
    const { value } = this.props;
    const { intl } = this.context;

    return (
      <div className="row toolbar__search__input col-xs">
        <input
          className="input input--search col-xs"
          type="search"
          ref="search"
          tabIndex="1"
          value={value}
          placeholder={intl.messages['search.placeholder']}
          onFocus={this.props.onFocus}
          onChange={this.handleChange}
        />
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
