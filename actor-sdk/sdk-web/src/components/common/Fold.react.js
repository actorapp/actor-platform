/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

class Fold extends Component {
  static propTypes = {
    children: PropTypes.oneOfType([
      PropTypes.arrayOf(PropTypes.node),
      PropTypes.node
    ]),
    icon: PropTypes.string,
    iconClassName: PropTypes.string,
    iconElement: PropTypes.element,
    title: PropTypes.string.isRequired
  };

  constructor(props) {
    super(props);

    this.state = {
      isOpen: false
    }
  }

  onClick = () => this.setState({ isOpen: !this.state.isOpen });

  render() {
    const { icon, iconClassName, title, iconElement } = this.props;
    const titleIconClassName = classnames('material-icons icon', iconClassName);
    const className = classnames({
      'fold': true,
      'fold--open': this.state.isOpen
    });

    let foldIcon;
    if (icon) {
      foldIcon = <i className={titleIconClassName}>{icon}</i>;
    }
    if (iconElement) {
      foldIcon = iconElement;
    }

    return (
      <div className={className}>
        <div className="fold__title" onClick={this.onClick}>
          {foldIcon}
          {title}
          <i className="fold__indicator material-icons pull-right">arrow_drop_down</i>
        </div>
        <div className="fold__content">
          {this.props.children}
        </div>
      </div>
    );
  }
}

export default Fold;
