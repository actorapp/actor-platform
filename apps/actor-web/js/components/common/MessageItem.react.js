var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var classNames = require('classnames');
var hljs = require('highlight.js');
var marked = require('marked');
var memoize = require('memoizee');
var memoizedMarked = memoize(marked, {length: 1, maxAge: 60 * 60 * 1000, max: 1000}); // 1h expire, max 1000 elements

var AvatarItem = require('./AvatarItem.react');

var MessageItem = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    message: React.PropTypes.object.isRequired
  },

  _markedOptions: {
    sanitize: true,
    breaks: true,
    highlight: function (code) {
      return hljs.highlightAuto(code).value;
    }
  },

  componentWillMount: function() {
    this._renderTextContent(this.props);
  },

  componentWillReceiveProps: function(props) {
    this._renderTextContent(props);
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

    if (message.content.content == 'service') {
      avatar = null;
      header = null;
    }

    return(
      <li className="message row">
        {avatar}
        <div className="message__body col-xs">
          {header}
          <MessageItem.Content content={message.content}/>
        </div>
      </li>
    );
  },

  _renderTextContent: function(props) {
    if (props.message.content.content == 'text') {
      props.message.content.html = memoizedMarked(props.message.content.text, this._markedOptions);
    }
  }

});

MessageItem.Content = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    content: React.PropTypes.object.isRequired
  },

  render: function() {
    var content = this.props.content;
    var contentClassName = classNames('message__content', {
      'message__content--service': content.content == 'service',
      'message__content--text': content.content == 'text',
      'message__content--photo': content.content == 'photo',
      'message__content--document': content.content == 'document',
      'message__content--unsupported': content.content == 'unsupported'
    });

    switch (content.content) {
      case 'service':
        return (
          <div className={contentClassName}>
            {content.text}
          </div>
        );
      case 'text':
        return (
          <div className={contentClassName}
               dangerouslySetInnerHTML={{__html: content.html}}>
          </div>
        );
      case 'photo':
        var original = null;
        var preview = <img className="photo photo--preview"
                           width={content.w}
                           height={content.h}
                           src={content.preview}/>;
        if (content.fileUrl) {
          original = <img className="photo photo--original"
                          width={content.w}
                          height={content.h}
                          src={content.fileUrl}/>;
        }
        return (
          <div className={contentClassName}>
            {original}
            {preview}
          </div>
        );
      case 'document':
        return (
          <div className={contentClassName}>
            <a className="document" href={content.fileUrl}>
              <img className="document__icon" src="assets/img/icons/ic_attach_file_24px.svg"/>
              <span className="document__filename">{content.fileName}</span>
            </a>
          </div>
        );
      default:
    }
  }
});

module.exports = MessageItem;
