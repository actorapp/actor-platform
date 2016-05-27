/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { KeyCodes } from '../../constants/ActorAppConstants';

class DialogSearch extends Component {
  static propTypes = {
    isOpen: PropTypes.bool.isRequired,
    query: PropTypes.string.isRequired,
    onCancel: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.handleCancel = this.handleCancel.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
    this.handleQueryChange = this.handleQueryChange.bind(this);
  }

  shouldComponentUpdate(nextProps) {
    return nextProps.isOpen !== this.props.isOpen ||
           nextProps.query !== this.props.query;
  }

  componentDidUpdate(prevProps) {
    if (this.props.isOpen && !prevProps.isOpen) {
      this.setCaretToEnd();
    }
  }

  handleCancel() {
    this.props.onCancel();
  }

  handleKeyDown(event) {
    if (event.keyCode === KeyCodes.ESC) {
      event.preventDefault();
      this.handleCancel();
    }
  }

  handleQueryChange(event) {
    this.props.onChange(event.target.value);
  }

  renderContent() {
    const { query } = this.props;

    return (
      <div className="dialog__search row">
        <input
          className="dialog__search__input"
          type="search"
          ref="input"
          placeholder="Search messages in this dialog"
          value={query}
          onChange={this.handleQueryChange}
          onKeyDown={this.handleKeyDown}
        />
        <div className="dialog__search__controls">
          <a className="dialog__search__cancel link link--blue" onClick={this.handleCancel}>
            Cancel
          </a>
        </div>
      </div>
    );
  }

  render() {
    const { isOpen } = this.props;

    return (
      <div className="dialog__search__container">
        {isOpen ? this.renderContent() : null}
      </div>
    );
  }

  focus() {
    const { input } = this.refs;
    if (input) {
      input.focus();
    }
  }

  setCaretToEnd() {
    const { input } = this.refs;
    if (input) {
      input.focus();
      input.selectionStart = input.selectionEnd = input.value.length;
    }
  }
}

export default DialogSearch;
