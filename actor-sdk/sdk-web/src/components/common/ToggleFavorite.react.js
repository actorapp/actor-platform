/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */


import React, { Component, PropTypes } from 'react';

class ToggleFavorite extends Component {
  static propTypes = {
    value: PropTypes.bool.isRequired,
    onToggle: PropTypes.func.isRequired
  };

  render() {
    const { value } = this.props;
    const glyph = value ? 'star' : 'star_border';

    return (
      <i className="material-icons" onClick={this.props.onToggle}>
        {glyph}
      </i>
    );
  }
}

export default ToggleFavorite;
