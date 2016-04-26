/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

class SidebarButton extends Component {
  static propTypes = {
    title: PropTypes.node.isRequired,
    glyph: PropTypes.string.isRequired,
    onClick: PropTypes.func.isRequired
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  render() {
    const { title, glyph, onClick } = this.props;

    return (
      <div className="recent__history" onClick={onClick}>
        <div className="recent__history__icon">
          <i className="material-icons">{glyph}</i>
        </div>
        <div className="recent__history__title">
          {title}
        </div>
      </div>
    );
  }
}

export default SidebarButton;
