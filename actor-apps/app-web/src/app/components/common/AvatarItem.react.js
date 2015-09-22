/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import classNames from 'classnames';

class AvatarItem extends React.Component {
  static propTypes = {
    className: React.PropTypes.string,
    image: React.PropTypes.string,
    placeholder: React.PropTypes.string.isRequired,
    size: React.PropTypes.string,
    title: React.PropTypes.string.isRequired
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { title, className, image, size, placeholder } = this.props;

    const placeholderClassName = classNames('avatar__placeholder', `avatar__placeholder--${placeholder}`);
    const avatarClassName = classNames('avatar', {
      'avatar--tiny': size === 'tiny',
      'avatar--small': size === 'small',
      'avatar--medium': size === 'medium',
      'avatar--large': size === 'large',
      'avatar--big': size === 'big',
      'avatar--huge': size === 'huge'
    }, className);

    const avatar = image ? <img alt={title} className="avatar__image" src={image}/> : null;

    return (
      <div className={avatarClassName}>
        {avatar}
        <span className={placeholderClassName}>{title[0]}</span>
      </div>
    );
  }
}

export default AvatarItem;
