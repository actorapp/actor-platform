var React = require('react');

var AvatarItem = require('../common/AvatarItem.react');
var RecentSectionActionCreators = require('../../actions/sidebar/RecentSectionActionCreators.react');

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
    RecentSectionActionCreators.selectPeer(this.props.dialog.peer);
  }
});

module.exports = RecentSectionItem;
