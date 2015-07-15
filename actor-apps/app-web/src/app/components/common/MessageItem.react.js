import _ from 'lodash';

import React from 'react';
import { PureRenderMixin } from 'react/addons';

import memoize from 'memoizee';
import classNames from 'classnames';
import emojify from 'emojify.js';
import hljs from 'highlight.js';
import marked from 'marked';
import emojiCharacters from 'emoji-named-characters';
import Lightbox from 'jsonlylightbox';

import VisibilitySensor from 'react-visibility-sensor';

import AvatarItem from './AvatarItem.react';

import DialogActionCreators from '../../actions/DialogActionCreators';
import { MessageContentTypes } from '../../constants/ActorAppConstants';

let lastMessageSenderId,
    lastMessageContentType;

var MessageItem = React.createClass({
  displayName: 'MessageItem',

  propTypes: {
    message: React.PropTypes.object.isRequired,
    onVisibilityChange: React.PropTypes.func
  },

  mixins: [PureRenderMixin],

  onClick() {
    DialogActionCreators.selectDialogPeerUser(this.props.message.sender.peer.id);
  },

  onVisibilityChange(isVisible) {
    this.props.onVisibilityChange(this.props.message, isVisible);
  },

  render() {
    const message = this.props.message;

    let header,
        visibilitySensor,
        leftBlock;

    let isSameSender = message.sender.peer.id === lastMessageSenderId && lastMessageContentType !== MessageContentTypes.SERVICE;

    let messageClassName = classNames({
      'message': true,
      'row': true,
      'message--same-sender': isSameSender
    });

    if (isSameSender) {
      leftBlock = (
        <div className="message__info text-right">
          <time className="message__timestamp">{message.date}</time>
          <MessageItem.State message={message}/>
        </div>
      );
    } else {
      leftBlock = (
        <div className="message__info message__info--avatar">
          <a onClick={this.onClick}>
            <AvatarItem image={message.sender.avatar}
                        placeholder={message.sender.placeholder}
                        title={message.sender.title}/>
          </a>
        </div>
      );
      header = (
        <header className="message__header">
          <h3 className="message__sender">
            <a onClick={this.onClick}>{message.sender.title}</a>
          </h3>
          <time className="message__timestamp">{message.date}</time>
          <MessageItem.State message={message}/>
        </header>
      );

    }

    if (message.content.content === MessageContentTypes.SERVICE) {
      leftBlock = null;
      header = null;
    }

    if (this.props.onVisibilityChange) {
      visibilitySensor = <VisibilitySensor onChange={this.onVisibilityChange}/>;
    }

    lastMessageSenderId = message.sender.peer.id;
    lastMessageContentType = message.content.content;

    return (
      <li className={messageClassName}>
        {leftBlock}
        <div className="message__body col-xs">
          {header}
          <MessageItem.Content content={message.content}/>
          {visibilitySensor}
        </div>
      </li>
    );
  }

});


emojify.setConfig({
  mode: 'img',
  img_dir: 'assets/img/emoji' // eslint-disable-line
});

const mdRenderer = new marked.Renderer();
// target _blank for links
mdRenderer.link = function(href, title, text) {
  let external, newWindow, out;
  external = /^https?:\/\/.+$/.test(href);
  newWindow = external || title === 'newWindow';
  out = '<a href=\"' + href + '\"';
  if (newWindow) {
    out += ' target="_blank"';
  }
  if (title && title !== 'newWindow') {
    out += ' title=\"' + title + '\"';
  }
  return (out + '>' + text + '</a>');
};

const markedOptions = {
  sanitize: true,
  breaks: true,
  highlight: function (code) {
    return hljs.highlightAuto(code).value;
  },
  renderer: mdRenderer
};


const inversedEmojiCharacters = _.invert(_.mapValues(emojiCharacters, (e) => e.character));

const emojiVariants = _.map(Object.keys(inversedEmojiCharacters), function(name) {
  return name.replace(/\+/g, '\\+');
});

const emojiRegexp = new RegExp('(' + emojiVariants.join('|') + ')', 'gi');

const processText = function(text) {
  let markedText = marked(text, markedOptions);

  // need hack with replace because of https://github.com/Ranks/emojify.js/issues/127
  const noPTag = markedText.replace(/<p>/g, '<p> ');

  let emojifiedText = emojify
    .replace(noPTag.replace(emojiRegexp, (match) => ':' + inversedEmojiCharacters[match] + ':'));

  return emojifiedText;
};

const memoizedProcessText = memoize(processText, {
    length: 1000,
    maxAge: 60 * 60 * 1000,
    max: 10000
});

// lightbox init
const lightbox = new Lightbox();
const lightboxOptions = {
  animation: false,
  controlClose: '<i class="material-icons">close</i>'
};
lightbox.load(lightboxOptions);

MessageItem.Content = React.createClass({
  propTypes: {
    content: React.PropTypes.object.isRequired
  },

  mixins: [PureRenderMixin],

  getInitialState() {
    return {
      isImageLoaded: false
    };
  },

  render() {
    const content = this.props.content;
    const isImageLoaded = this.state.isImageLoaded;
    let contentClassName = classNames('message__content', {
      'message__content--service': content.content === MessageContentTypes.SERVICE,
      'message__content--text': content.content === MessageContentTypes.TEXT,
      'message__content--photo': content.content === MessageContentTypes.PHOTO,
      'message__content--photo--loaded': isImageLoaded,
      'message__content--document': content.content === MessageContentTypes.DOCUMENT,
      'message__content--unsupported': content.content === MessageContentTypes.UNSUPPORTED
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
               dangerouslySetInnerHTML={{__html: memoizedProcessText(content.text)}}>
          </div>
        );
      case 'photo':
        const preview = <img className="photo photo--preview" src={content.preview}/>;
        const k = content.w / 300;
        const photoMessageStyes = {
          width: Math.round(content.w / k),
          height: Math.round(content.h / k)
        };

        let original,
            preloader;

        if (content.fileUrl) {
          original = (
            <img className="photo photo--original"
                 height={content.h}
                 onClick={this.openLightBox}
                 onLoad={this.imageLoaded}
                 src={content.fileUrl}
                 width={content.w}/>
          );
        }

        if (content.isUploading === true || isImageLoaded === false) {
          preloader =
            <div className="preloader"><div/><div/><div/><div/><div/></div>;
        }

        return (
          <div className={contentClassName} style={photoMessageStyes}>
            {preview}
            {original}
            {preloader}
            <svg dangerouslySetInnerHTML={{__html: '<filter id="blur-effect"><feGaussianBlur stdDeviation="3"/></filter>'}}></svg>
          </div>
        );
      case 'document':
        contentClassName = classNames(contentClassName, 'row');

        let availableActions;
        if (content.isUploading === true) {
          availableActions = <span>Loading...</span>;
        } else {
          availableActions = <a href={content.fileUrl}>Open</a>;
        }

        return (
          <div className={contentClassName}>
            <div className="document row">
              <div className="document__icon">
                <i className="material-icons">attach_file</i>
              </div>
              <div className="col-xs">
                <span className="document__filename">{content.fileName}</span>
                <div className="document__meta">
                  <span className="document__meta__size">{content.fileSize}</span>
                  <span className="document__meta__ext">{content.fileExtension}</span>
                </div>
                <div className="document__actions">
                  {availableActions}
                </div>
              </div>
            </div>
            <div className="col-xs"></div>
          </div>
        );
      default:
    }
  },

  imageLoaded() {
    this.setState({isImageLoaded: true});
  },

  openLightBox() {
    lightbox.open(this.props.content.fileUrl);
  }
});

MessageItem.State = React.createClass({
  propTypes: {
    message: React.PropTypes.object.isRequired
  },

  render() {
    const message = this.props.message;

    if (message.content.content === MessageContentTypes.SERVICE) {
      return null;
    } else {
      let icon = null;

      switch(message.state) {
        case 'pending':
          icon = <i className="status status--penging material-icons">access_time</i>;
          break;
        case 'sent':
          icon = <i className="status status--sent material-icons">done</i>;
          break;
        case 'received':
          icon = <i className="status status--received material-icons">done_all</i>;
          break;
        case 'read':
          icon = <i className="status status--read material-icons">done_all</i>;
          break;
        case 'error':
          icon = <i className="status status--error material-icons">report_problem</i>;
          break;
        default:
      }

      return (
        <div className="message__status">{icon}</div>
      );
    }
  }
});

export default MessageItem;
