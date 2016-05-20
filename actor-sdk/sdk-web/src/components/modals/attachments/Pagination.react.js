/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

export default class Pagination extends Component {
  static propTypes = {
    current: PropTypes.number.isRequired,
    total: PropTypes.number.isRequired,
    onChange: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isAtStart: props.current === 0,
      isAtEnd: props.current === props.total
    }

    this.handlePrevClick = this.handlePrevClick.bind(this);
    this.handleNextClick = this.handleNextClick.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      isAtStart: nextProps.current === 0,
      isAtEnd: nextProps.current === nextProps.total
    })
  }

  handlePrevClick() {
    const { current, onChange } = this.props;

    if (current !== 0) {
      onChange(current - 1);
    }
  }

  handleNextClick() {
    const { current, total, onChange } = this.props;

    if (current !== total) {
      onChange(current + 1);
    }
  }

  renderPrevArrow() {
    const { isAtStart } = this.state;

    if (isAtStart) {
      return (
        <div className="pagination__control pagination__control--disabled">
          <i className="material-icons">keyboard_arrow_left</i>
        </div>
      );
    }

    return (
      <div className="pagination__control" onClick={this.handlePrevClick}>
        <i className="material-icons">keyboard_arrow_left</i>
      </div>
    );
  }

  renderPager() {
    const { current, total } = this.props;

    return (
      <div className="pagination__pager">
        {current + 1} / {total + 1}
      </div>
    );
  }

  renderNextArrow() {
    const { isAtEnd } = this.state;

    if (isAtEnd) {
      return (
        <div className="pagination__control pagination__control--disabled">
          <i className="material-icons">keyboard_arrow_right</i>
        </div>
      );
    }

    return (
      <div className="pagination__control" onClick={this.handleNextClick}>
        <i className="material-icons">keyboard_arrow_right</i>
      </div>
    );
  }

  render() {
    return (
      <div className="pagination">
        {this.renderPrevArrow()}
        {this.renderPager()}
        {this.renderNextArrow()}
      </div>
    )
  }
}
