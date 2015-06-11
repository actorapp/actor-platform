import React from 'react';
import classNames from 'classnames';

class AvatarItem extends React.Component {
  constructor() {
    super();
  }

  render() {
    let title = this.props.title;
    let image = this.props.image;
    let size = this.props.size;
    let placeholderClassName = classNames('avatar__placeholder', `avatar__placeholder--${this.props.placeholder}`);
    let avatarClassName = classNames('avatar', {
      'avatar--small': size === 'small',
      'avatar--square': size === 'square',
      'avatar--huge': size === 'huge',
      'avatar--tiny': size === 'tiny'
    });

    let placeholder;
    if (size === 'square') {
      placeholder = <span className={placeholderClassName}></span>;
    } else {
      placeholder = <span className={placeholderClassName}>{title[0]}</span>;
    }

    let avatar;
    if (image) {
      avatar = <img alt={title} className="avatar__image" src={image}/>;
    }

    return (
      <div className={avatarClassName}>
        {avatar}
        {placeholder}
      </div>
    );
  }
}

AvatarItem.propTypes = {
  image: React.PropTypes.string,
  placeholder: React.PropTypes.string,
  size: React.PropTypes.string,
  title: React.PropTypes.string.isRequired
};

export default AvatarItem;
