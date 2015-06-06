var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var hljs = require('highlight.js');
var marked = require('marked');
var memoize = require('memoizee');
var Isvg = require('react-inlinesvg');
var memoizedMarked = memoize(marked, {length: 1, maxAge: 60 * 60 * 1000, max: 1000}); // 1h expire, max 1000 elements

var Message = React.createClass({
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

    return (
      <div className="message__body col-xs">
        <Message.State message={message}/>
        <Message.Content content={message.content}/>
      </div>
    );
  },

  _renderTextContent: function(props) {
    if (props.message.content.content == 'text') {
      props.message.content.html = memoizedMarked(props.message.content.text, this._markedOptions);
    }
  }
});

Message.Content = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    content: React.PropTypes.object.isRequired
  },
  render: function() {
    var content = this.props.content;

    switch(content.content) {
      case 'text':
        return (
          <div className="message__text"
               dangerouslySetInnerHTML={{__html: content.html}}>
          </div>
        );
      case 'document':
        return (
          <a href={content.fileUrl} className="message__document">
            <img className="message__document__icon"
                 src="assets/img/icons/ic_attach_file_24px.svg"></img>
            {content.fileName}
          </a>
        );
      case 'photo':
        var original = null;
        var preview = <img className="message__photo__preview"
                           width={content.w}
                           height={content.h}
                           src={content.preview}/>
        if (content.fileUrl) {
          original = <img className="message__photo__original"
                          width={ content.w }
                          height={ content.h }
                          src={content.fileUrl}/>
        }

        return (
          <div className="message__photo">
            {original}
            {preview}
          </div>
        );
      case 'service':
        return(
          <p className="message__service">
            {content.text}
          </p>
        );
      case 'default':
        return(
          <p className="message__unsupported">
            Данный вид контента на данный момент не поддерживается.
          </p>
        );
    }
  }
});

Message.State = React.createClass({
  propTypes: {
    message: React.PropTypes.object.isRequired
  },
  render: function() {
    var message = this.props.message;

    if (message.content.content == 'service') {
      return null;
    } else {
      var icon = null;

      switch(message.state) {
        case 'pending':
          //icon = <img src="assets/img/icons/ic_access_time_24px.svg"
          //            className="status status--penging"/>;
          icon = <Isvg className="status status--penging"
                       src="assets/img/icons/ic_access_time_24px.svg"></Isvg>
          break;
        case 'sent':
          //icon = <img src="assets/img/icons/ic_done_24px.svg"
          //            className="status status--sent"/>;
          icon = <Isvg className="status status--sent"
                       src="assets/img/icons/ic_done_24px.svg"></Isvg>
          break;
        case 'received':
          //icon = <img src="assets/img/icons/ic_done_all_24px.svg"
          //            className="status status--received"/>;
          icon = <Isvg className="status status--received"
                       src="assets/img/icons/ic_done_all_24px.svg"></Isvg>
          break;
        case 'read':
          //icon = <img src="assets/img/icons/ic_done_all_24px.svg"
          //            className="status status--read"/>;
          icon = <Isvg className="status status--read"
                       src="assets/img/icons/ic_done_all_24px.svg"></Isvg>
          break;
        case 'error':
          //icon = <img src="assets/img/icons/ic_report_problem_24px.svg"
          //            className="status status--error"/>;
          icon = <Isvg className="status status--error"
                       src="assets/img/icons/ic_report_problem_24px.svg"></Isvg>
          break;
      }

      return (
        <h3 key={message.state} layout="row" className="message__body__title row">
          <span className="col-xs">{message.sender.title}</span>

          <div className="message__status">{icon}</div>

          <time className="message__body__timestamp">{message.date}</time>
        </h3>
      );
    }
  }
});

module.exports = Message;
