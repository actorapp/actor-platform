import _ from 'lodash';

import marked from 'marked';
import hljs from 'highlight.js';

const plainTextRenderer = _.assign(new marked.Renderer({
  tables: false
}), {
  code: (code) => code + '\n\n',

  blockquote: (quote) => quote.replace(/^/gm, '>'),

  html: (html) => html,

  heading: (text) => text + '\n\n',

  hr: () => '--------------------',

  list: (body, ordered) => body,

  listitem: (text) => '– ' + text + '\n',

  paragraph: (text) => '\n  ' + text,

  strong: (text) => text,

  em: (text) => text,

  codespan: (text) => '`' + text + '`',

  br: () => '\n',

  del: (text) => text,

  link: (href, title, text) => '[' + title + '](' + href + ')',

  image: (href, title, text) => '[' + title + '](' + href + ')'
});

const defaultRenderer = _.assign(new marked.Renderer({
  sanitize: true,
  breaks: true,
  highlight: function (code) {
    return hljs.highlightAuto(code).value;
  }
}), {
  link: (href, title, text) => {
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
  }
});

export default {
  plainText: (text) => marked(text, {renderer: plainTextRenderer}),
  default: (text) => marked(text, {renderer: defaultRenderer})
};
