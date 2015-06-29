import React from 'react';
import classNames from 'classnames';

class AvatarItem extends React.Component {
  static propTypes = {
    image: React.PropTypes.string,
    placeholder: React.PropTypes.string.isRequired,
    size: React.PropTypes.string,
    title: React.PropTypes.string.isRequired
  };

  constructor() {
    super();
  }

  render() {
    const title = this.props.title;
    const image = this.props.image;
    const size = this.props.size;

    let placeholder,
        avatar;
    let placeholderClassName = classNames('avatar__placeholder', `avatar__placeholder--${this.props.placeholder}`);
    let avatarClassName = classNames('avatar', {
      'avatar--tiny': size === 'tiny',
      'avatar--small': size === 'small',
      'avatar--big': size === 'big',
      'avatar--huge': size === 'huge',
      'avatar--square': size === 'square'
    });

    //if (size === 'square') {
    //  placeholder = <span className={placeholderClassName}></span>;
    //} else {
      placeholder = <span className={placeholderClassName}>{title[0]}</span>;
    //}

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

export default AvatarItem;
