var React = require('react');

var AvatarItem = React.createClass({
  propTypes: {
    title: React.PropTypes.string.isRequired,
    image: React.PropTypes.string,
    placeholder: React.PropTypes.string
  },

  render: function() {
    var title = this.props.title;
    var image = this.props.image;
    var placeholder = this.props.placeholder;

    var avatar;

    if (image) {
      avatar = <img class="avatar__image" src={image} alt={title}/>
    } else {
      var char = title[0];
      var className = "avatar__placeholder avatar__placeholder--" + placeholder
      avatar = <span className={className}>{char}</span>
    }

    return(avatar);
  }
});

module.exports = AvatarItem;
