var React = require('react');
var classNames = require('classnames');

var AvatarItem = require('./AvatarItem.react');

var MessageItem = React.createClass({
  propTypes: {
    message: React.PropTypes.object.isRequired
  },

  render: function() {
    var message = this.props.message;
    var avatar =
      <AvatarItem title={message.sender.title}
                  image={message.sender.avatar}
                  placeholder={message.sender.placeholder}
                  size="small"/>;
    var header =
      <header className="message__header row">
        <h3 className="message__sender col-xs">{message.sender.title}</h3>
        <span className="message__status"></span>
        <time className="message__timestamp">{message.date}</time>
      </header>;

    var content = null;
    var contentClassName = classNames('message__content', {
      'message__content--service': message.content.content == 'service',
      'message__content--text': message.content.content == 'text',
      'message__content--photo': message.content.content == 'photo',
      'message__content--document': message.content.content == 'document',
      'message__content--unsupported': message.content.content == 'unsupported'
    });

    switch(message.content.content) {
      case 'service':
        avatar = null;
        header = null;
        content = message.content.text;
        break;
      case 'text':
        content = message.content.text;
        break;
      case 'photo':
        var original = null;
        var preview = <img className="photo photo--preview"
                           width={message.content.w}
                           height={message.content.h}
                           src={message.content.preview}/>;
        if (message.content.fileUrl) {
          original = <img className="photo photo--original"
                          width={ message.content.w }
                          height={ message.content.h }
                          src={message.content.fileUrl}/>;
        }
        content = [original, preview];
        break;
      case 'document':
        content =
          <a className="document" href={message.content.fileUrl}>
            <img className="document__icon" src="assets/img/icons/ic_attach_file_24px.svg"/>
            <span className="document__filename">{message.content.fileName}</span>
          </a>;
        break;
      default:
    }

    return(
      <li className="message row">
        {avatar}
        <div className="message__body col-xs">
          {header}
          <div className={contentClassName}>
            {content}
          </div>
        </div>
      </li>
    );
  }
});

module.exports = MessageItem;
