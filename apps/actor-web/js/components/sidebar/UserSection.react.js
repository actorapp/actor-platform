var React = require('react');

var UserSection = React.createClass({
  render: function() {
    return(
      <div className="sidebar__header__user row">
        <div className="sidebar__header__user__avatar avatar avatar--small">
          <span className="avatar__placeholder avatar__placeholder--yellow">O</span>
        </div>
        <span className="sidebar__header__user__name">Oleg Shilov</span>
        <span className="col-xs"></span>
        <img className="sidebar__header__user__expand" src="assets/img/icons/png/ic_expand_more_2x_white.png" alt=""/>
      </div>
    );
  }
});

module.exports = UserSection;
