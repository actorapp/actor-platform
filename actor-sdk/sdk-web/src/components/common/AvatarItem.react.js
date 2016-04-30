/*
 * Copyright (C) 2015-2016 Actor LLC. <https://actor.im>
 */

import React, { Component, PropTypes } from 'react';
import classNames from 'classnames';

const AvatarSizes = {
  tiny: 24,
  small: 32,
  normal: 36,
  medium: 44,
  large: 60,
  big: 120,
  huge: 200
};

class AvatarItem extends Component {
  static propTypes = {
    image: PropTypes.string,
    placeholder: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    size: PropTypes.oneOf(Object.keys(AvatarSizes)).isRequired,
    className: PropTypes.string,
    onClick: PropTypes.func
  };

  static defaultProps = {
    size: 'normal'
  };

  shouldComponentUpdate(prevProps) {
    return prevProps.image !== this.props.image ||
           prevProps.placeholder !== this.props.placeholder ||
           prevProps.title !== this.props.title ||
           prevProps.size !== this.props.size ||
           prevProps.className !== this.props.className;
  }

  getFirstChar() {
    const { title } = this.props;
    const emojiFirstChar = /([\uE000-\uF8FF]|\uD83C|\uD83D)/g;

    if (title.length == 0) {
      return '#';
    }

    return title[0].match(emojiFirstChar) ? '#' : title[0];
  }

  render() {
    const { image, placeholder, title, size, onClick } = this.props;

    if (image) {
      const className = classNames(
        'avatar__image',
        { 'avatar--clickable': onClick },
        this.props.className
      );

      const imgSize = AvatarSizes[size];

      return (
        <img
          className={className}
          src={image}
          width={imgSize}
          height={imgSize}
          alt={title}
          onClick={onClick}
        />
      );
    }

    const className = classNames(
      'avatar__placeholder',
      `avatar__placeholder--${size}`,
      `avatar__placeholder--${placeholder}`,
      this.props.className,
      { 'avatar--clickable': onClick }
    );

    return (
      <div className={className} onClick={onClick} title={title}>
        {this.getFirstChar()}
      </div>
    );
  }
}

export default AvatarItem;
