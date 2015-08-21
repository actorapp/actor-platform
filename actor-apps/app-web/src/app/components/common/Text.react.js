import _ from 'lodash';

import React from 'react';

import memoize from 'memoizee';
import emojify from 'emojify.js';
import hljs from 'highlight.js';
import marked from 'marked';
import emojiCharacters from 'emoji-named-characters';

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

class Text extends React.Component {
  static propTypes = {
    content: React.PropTypes.object.isRequired,
    className: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { content, className } = this.props;

    return (
      <div className={className}
           dangerouslySetInnerHTML={{__html: memoizedProcessText(content.text)}}>
      </div>
    );
  }
}

export default Text;
