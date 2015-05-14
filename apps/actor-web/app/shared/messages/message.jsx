var Message = React.createClass({
  propTypes: {
    message: React.PropTypes.object.isRequired
  },
  render: function() {
    var message = this.props.message;

    return (
      <div className="md-list-item-text messages-list__item__body" flex>
        <Message.State message={message}/>
        <Message.Content content={message.content}/>
      </div>
    );
  }
});

Message.Content = React.createClass({
  findUriExp: /\b((?:[a-z][\w-]+:(?:\/{1,3}|[a-z0-9%])|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}\/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'".,<>?\u00ab\u00bb\u201c\u201d\u2018\u2019]))/gi,
  nlExp: /[\r\n]+/g,
  propTypes: {
    content: React.PropTypes.object.isRequired
  },
  render: function() {
    var content = this.props.content;

    switch(content.content) {
      case 'text':
        var text = this.renderText(content.text);

        return (
          <p>{text}</p>
        );
      default:
        return <p>Unsupported content</p>;
    }
  },
  // renders text with nl2br and linkify
  renderText: function(text) {
    var result = [];

    var nld = this.nl2br(text);

    var _this = this;
    nld.forEach(function(line) {
      if (typeof line === 'string') {
        _this.linkify(line).forEach(function (linkifiedLine) {
          result.push(linkifiedLine);
        });
      } else {
        result.push(line);
      }
    });

    return result;
  },
  nl2br: function(text) {
    var split = text.split(this.nlExp);
    if (split.length > 1) {
      var result = [];

      for (var i = 0; i < split.length; i++) {
        if (split[i] !== undefined) {
          result.push(split[i]);
          if (i + 1 < split.length) {
            result.push(<br/>)
          }
        }
      }

      return result;
    } else {
      return [text];
    }
  },
  linkify: function(text) {
    var split = text.split(this.findUriExp);
    var result = [];
    for (var i = 0; i < split.length; ++i) {
      if (split[i] !== undefined) {
        if (i + 1 < split.length && split[i + 1] === undefined) {
          result.push(<a href={split[i]} target="_blank">{split[i]}</a>);
        } else {
          result.push(split[i]);
        }
      }
    }
    return result;
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
          icon = <img src="assets/img/icons/ic_access_time_24px.svg" className="status status--penging"/>;
          break;
        case 'sent':
          icon = <img src="assets/img/icons/ic_done_24px.svg" className="status status--sent"/>;
          break;
        case 'received':
          icon = <img src="assets/img/icons/ic_done_all_24px.svg" className="status status--received"/>;
          break;
        case 'read':
          icon = <img src="assets/img/icons/ic_done_all_24px.svg" className="status status--read"/>;
          break;
        case 'error':
          icon = <img src="assets/img/icons/ic_report_problem_24px.svg" className="status status--error"/>;
          break;
      }

      return (
        <h3 layout="row" className="messages-list__item__body__title">
          <span flex>{message.sender.title}</span>

          <div className="messages-list__item__status">{icon}</div>

          <time className="messages-list__item__body__timestamp">{message.date}</time>
        </h3>
      );
    }
  }
});
