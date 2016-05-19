import React, { Component, PropTypes } from 'react';
import { findDOMNode } from 'react-dom';

import DialogSearchStore from '../../stores/DialogSearchStore';

import DialogSearchActionCreators from '../../actions/DialogSearchActionCreators';

class DialogSearch extends Component {
  static propTypes = {
    isOpen: PropTypes.bool.isRequired,
    query: PropTypes.string.isRequired,
    results: PropTypes.array.isRequired,

    onCancel: PropTypes.func.isRequired,
    onChange: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);
    console.debug(props);

    this.handleCancelClick = this.handleCancelClick.bind(this);
    this.handleQueryChange = this.handleQueryChange.bind(this);
  }

  componentDidMount() {
    findDOMNode(this.refs.search).focus();
  }

  handleCancelClick() {
    const { onCancel } = this.props;

    onCancel();
  }

  handleQueryChange(event) {
    const { onChange } = this.props;

    onChange(event.target.value);
  }

  render() {
    const { isOpen, query, results } = this.props;

    return (
      <div className="dialog__search row">
        <div className="dialog__search__input col-xs">
          <input
            type="search"
            ref="search"
            placeholder="Search messages in this dialog"
            value={query}
            onChange={this.handleQueryChange}
          />
          <div className="dialog__search__filter">
            <i className="material-icons">message</i>
            <i className="material-icons">photo</i>
            <i className="material-icons">link</i>
          </div>
        </div>
        <div className="dialog__search__controls">
          <a className="link link--blue" onClick={this.handleCancelClick}>
            Cancel
          </a>
        </div>
      </div>
    );
  }
}

export default DialogSearch;
