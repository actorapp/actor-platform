'use strict';

var _ = require('lodash');

var React = require('react');
var PureRenderMixin = require('react/addons').addons.PureRenderMixin;

var DialogActionCreators = require('../../actions/DialogActionCreators');

var AvatarItem = require('../common/AvatarItem.react');

var GroupProfile = React.createClass({
  propTypes: {
    group: React.PropTypes.object.isRequired
  },

  render: function() {
    var group = this.props.group;

    return(
      <div className="activity__body">
        <AvatarItem title={group.name}
                    image={group.avatar}
                    placeholder={group.placeholder}
                    size="huge"/>

        <h3>{group.name}</h3>

        <GroupProfile.Members members={group.members}/>

        <a className="button">Add participant</a>
        <a className="button">Leave conversation</a>
      </div>
    );
  },
});

GroupProfile.Members = React.createClass({
  mixins: [PureRenderMixin],

  propTypes: {
    members: React.PropTypes.array.isRequired
  },

  render: function () {
    var members = this.props.members;

    var membersList = _.map(members, function(member, index) {
      return (
        <li key={index} className="row">
          <a onClick={this._onClick.bind(this, member.peerInfo.peer.id)}>
            <AvatarItem title={member.peerInfo.title}
                      image={member.peerInfo.avatar}
                      placeholder={member.peerInfo.placeholder}
                      size="tiny"/>
          </a>

          <div className="col-xs">
            <a onClick={this._onClick.bind(this, member.peerInfo.peer.id)}>{member.peerInfo.title}</a>
          </div>

          <div className="controls">
            <a><i className="material-icons">clear</i></a>
          </div>
        </li>
      );
    }, this);

    return (
      <ul className="activity__body__list activity__body__list--users">
        {membersList}
      </ul>
    );
  },

  _onClick: function(id) {
    DialogActionCreators.selectDialogPeerUser(id);
  }

});

module.exports = GroupProfile;
