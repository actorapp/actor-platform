/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classnames from 'classnames';

export default class AvatarItem extends Component {
  static propTypes = {
    className: PropTypes.string,
    image: PropTypes.string,
    placeholder: PropTypes.string.isRequired,
    size: PropTypes.string,
    title: PropTypes.string.isRequired,

    onClick: PropTypes.func
  };

  constructor(props) {
    super(props);
  }

  handleClick = (event) => {
    const { onClick } = this.props;
    onClick && onClick(event);
  };

  render() {
    const { title, className, image, size, placeholder } = this.props;

    const placeholderClassName = classnames('avatar__placeholder', `avatar__placeholder--${placeholder}`);
    const avatarClassName = classnames('avatar', {
      'avatar--tiny': size === 'tiny',
      'avatar--small': size === 'small',
      'avatar--medium': size === 'medium',
      'avatar--large': size === 'large',
      'avatar--big': size === 'big',
      'avatar--huge': size === 'huge',
      'avatar--without-shadow': !image
    }, className);

    const avatar = image ? <img alt={title} className="avatar__image" src={image}/> : null;

    const emojiFirstChar = /([\uE000-\uF8FF]|\uD83C|\uD83D)/g;

    let placeholderChar;
    if (title.length == 0) {
      placeholderChar = '#'
    } else {
      placeholderChar = title[0].match(emojiFirstChar) ? '#' : title[0];
    }

    return (
      <div className={avatarClassName} onClick={this.handleClick}>
        {avatar}
        <span className={placeholderClassName}>{placeholderChar}</span>
      </div>
    );
  }
}
