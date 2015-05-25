var React = require('react');

var AvatarItem = require('../common/AvatarItem.react');
var DialogActionCreators = require('../../actions/DialogActionCreators.react');

var RecentSectionItem = React.createClass({
  propTypes: {
    dialog: React.PropTypes.object.isRequired
  },

  render: function() {
    var dialog = this.props.dialog;

    return(
      <li className="sidebar__list__item" onClick={this._onClick}>
        <AvatarItem title={dialog.peer.title} image={dialog.peer.avatar} placeholder={dialog.peer.placeholder} size="tiny"/>
          <span>
            {dialog.peer.title}
          </span>
      </li>
    )
  },

  _onClick: function() {
    DialogActionCreators.selectDialog(this.props.dialog);
  }
});

module.exports = RecentSectionItem;
