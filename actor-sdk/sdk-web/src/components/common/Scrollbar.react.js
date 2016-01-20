/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';
import SimpleScroll from 'simple-scrollbar';

class Scrollbar extends Component {
  static propTypes = {
    children: PropTypes.oneOfType([
      PropTypes.element,
      PropTypes.array
    ]),
    className: PropTypes.string,

    onScroll: PropTypes.func
  };

  constructor(props) {
    super(props);
    this.scrollbar = new SimpleScroll();
  }

  componentDidMount() {
    const scrollNode = React.findDOMNode(this.refs.scroll);
    this.scrollbar.initElement(scrollNode);
  }

  handleScroll = (event) => {
    const { onScroll } = this.props;
    onScroll && onScroll(event);
  };

  scrollTo = (to) => {
    const scrollNode = React.findDOMNode(this.refs.scroll);
    this.scrollbar.scrollTo(scrollNode, to);
  };

  render() {
    const { children, className } = this.props;
    const wrapperClassName = classnames('scroll-wrapper', className);

    return (
      <div className={wrapperClassName} ref="scroll" onScroll={this.handleScroll}>
        {children}
      </div>
    );
  }
}

export default Scrollbar;
