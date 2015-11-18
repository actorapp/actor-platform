/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';

export default class Pagination extends Component {
  static propTypes = {
    current: PropTypes.number.isRequired,
    total: PropTypes.number.isRequire,
    onChange: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isAtStart: props.current === 0,
      isAtEnd: props.current === props.total
    }
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      isAtStart: nextProps.current === 0,
      isAtEnd: nextProps.current === nextProps.total
    })
  }

  handlePrevClick = () => {
    const { current, onChange } = this.props;

    if (current !== 0) {
      onChange(current - 1);
    }
  };
  handleNextClick = () => {
    const { current, total, onChange } = this.props;

    if (current !== total) {
      onChange(current + 1);
    }
  };

  render() {
    const { isAtStart, isAtEnd } = this.state;
    const { current, total } = this.props;

    return (
      <div className="pagination">
        {
          isAtStart
            ? <div className="pagination__control pagination__control--disabled">
                <i className="material-icons">keyboard_arrow_left</i>
              </div>
            : <div className="pagination__control" onClick={this.handlePrevClick}>
                <i className="material-icons">keyboard_arrow_left</i>
              </div>
        }

        <div className="pagination__pager">
          {current + 1} / {total + 1}
        </div>

        {
          isAtEnd
            ? <div className="pagination__control pagination__control--disabled">
                <i className="material-icons">keyboard_arrow_right</i>
              </div>
            : <div className="pagination__control" onClick={this.handleNextClick}>
                <i className="material-icons">keyboard_arrow_right</i>
              </div>
        }
      </div>
    )
  }
}
