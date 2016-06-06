/*
* Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
*/

import React, { Component, PropTypes } from 'react';

class Checkbox extends Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    name: PropTypes.string.isRequired,
    value: PropTypes.bool.isRequired,
    label: PropTypes.node.isRequired,
    onChange: PropTypes.func.isRequired
  };

  render() {
    return (
      <div className="checkbox">
        <input
          type="checkbox"
          id={this.props.id}
          name={this.props.name}
          checked={this.props.value}
          onChange={this.props.onChange}
        />
        <label htmlFor={this.props.id}>
          {this.props.label}
        </label>
      </div>
    );
  }
}

export default Checkbox;
