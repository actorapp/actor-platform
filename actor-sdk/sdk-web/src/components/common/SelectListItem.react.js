/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { SelectContextType } from './SelectList.react';

class SelectListItem extends Component {
  static contextTypes = {
    select: SelectContextType.isRequired
  };

  static propTypes = {
    index: PropTypes.number.isRequired,
    children: PropTypes.node.isRequired
  };

  constructor(props, context) {
    super(props, context);

    this.handleClick = this.handleClick.bind(this);
    this.handleMouseOver = this.handleMouseOver.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState, nextContext) {
    return nextContext.select.current !== this.context.select.current ||
           nextProps.index !== this.props.index ||
           nextProps.children !== this.props.children;
  }

  handleClick(event) {
    event.preventDefault();
    event.stopPropagation();

    this.context.select.pick();
  }

  handleMouseOver() {
    this.context.select.setCurrent(this.props.index);
  }

  render() {
    const { index, children } = this.props;
    const isSelected = index === this.context.select.current;
    const className = isSelected && 'selected';

    return (
      <div className={className} onClick={this.handleClick} onMouseOver={this.handleMouseOver}>
        {children}
      </div>
    );
  }
}

export default SelectListItem;
