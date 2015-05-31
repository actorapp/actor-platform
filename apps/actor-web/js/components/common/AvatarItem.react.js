var React = require('react');
var classNames = require('classnames');

var AvatarItem = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired,
    image: React.PropTypes.string,
    placeholder: React.PropTypes.string,
    size: React.PropTypes.string
  },

  render: function() {
    var title = this.props.title;
    var image = this.props.image;
    var size = this.props.size;
    var placeholderClassName = "avatar__placeholder avatar__placeholder--" + this.props.placeholder;
    var avatarClassName = classNames('avatar', {
      'avatar--small': size == 'small',
      'avatar--square': size == 'square',
      'avatar--tiny': size == 'tiny'
    });

    var placeholder;
    if (size == "square") {
      placeholder = <span className={placeholderClassName}></span>;
    } else {
      placeholder = <span className={placeholderClassName}>{title[0]}</span>
    }

    var avatar;
    if (image) {
      avatar = <img className="avatar__image" src={image} alt={title}/>
    }

    return (
      <div className={avatarClassName}>
        {avatar}
        {placeholder}
      </div>
    );
  }
});

module.exports = AvatarItem;
