/*
 * Copyright (C) 2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import { Link } from 'react-router';
import classnames from 'classnames';
import { shouldComponentUpdate } from 'react-addons-pure-render-mixin';

class SidebarLink extends Component {
  static propTypes = {
    to: PropTypes.string.isRequired,
    title: PropTypes.node.isRequired,
    glyph: PropTypes.string.isRequired,
    className: PropTypes.string,
    onlyActiveOnIndex: PropTypes.bool
  };

  constructor(props) {
    super(props);

    this.shouldComponentUpdate = shouldComponentUpdate.bind(this);
  }

  render() {
    const { to, title, glyph, className, onlyActiveOnIndex } = this.props;
    const buttonClassName = classnames('sidebar__button', className);

    return (
      <Link
        to={to}
        className={buttonClassName}
        activeClassName="sidebar__button--active"
        onlyActiveOnIndex={onlyActiveOnIndex || false}
      >
        <div className="sidebar__button__icon">
          <i className="material-icons">{glyph}</i>
        </div>
        <div className="sidebar__button__title">
          {title}
        </div>
      </Link>
    );
  }
}

export default SidebarLink;
