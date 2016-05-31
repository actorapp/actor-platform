/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { KeyCodes } from '../../constants/ActorAppConstants';

export const SelectContextType = PropTypes.shape({
  current: PropTypes.number.isRequired,
  setCurrent: PropTypes.func.isRequired
});

class SelectList extends Component {
  static childContextTypes = {
    select: SelectContextType
  };

  static propTypes = {
    className: PropTypes.string,
    max: PropTypes.number.isRequired,
    children: PropTypes.node.isRequired,
    onSelect: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      current: 0
    };

    this.setCurrent = this.setCurrent.bind(this);
    this.handleSelect = this.handleSelect.bind(this);
    this.handleKeyDown = this.handleKeyDown.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return nextState.current !== this.state.current ||
           nextProps.children !== this.props.children ||
           nextProps.max !== this.props.max ||
           nextProps.className !== this.props.className;
  }

  getChildContext() {
    return {
      select: {
        pick: this.handleSelect,
        current: this.state.current,
        setCurrent: this.setCurrent
      }
    };
  }

  componentDidMount() {
    document.addEventListener('keydown', this.handleKeyDown, false);
  }

  componentWillUnmount() {
    document.removeEventListener('keydown', this.handleKeyDown, false);
  }

  handleSelect() {
    this.props.onSelect(this.state.current);
  }

  handleNext() {
    const next = this.state.current + 1;
    this.setCurrent(next > this.props.max ? 0 : next);
  }

  handlePrevious() {
    const next = this.state.current - 1;
    this.setCurrent(next < 0 ? this.props.max : next);
  }

  handleKeyDown(event) {
    switch (event.keyCode) {
      case KeyCodes.ENTER:
        this.handleSelect();
        break;

      case KeyCodes.ARROW_UP:
        this.handlePrevious();
        break;

      case KeyCodes.TAB:
      case KeyCodes.ARROW_DOWN:
        this.handleNext();
        break;

      default:
        return;
    }

    event.stopPropagation();
    event.preventDefault();
  }

  render() {
    return (
      <div className={this.props.className}>
        {this.props.children}
      </div>
    )
  }

  setCurrent(current) {
    this.setState({ current });
  }
}

export default SelectList;
