var React = require('react');

var RecentSection = React.createClass({
  render: function() {
    return(
      <ul className="sidebar__list">
        <li className="sidebar__list__title">
          Recent
        </li>
        <li className="sidebar__list__item">
          <div className="avatar avatar--tiny">
            <span className="avatar__placeholder avatar__placeholder--blue">R</span>
          </div>
        <span>
          Recent conversation 1
        </span>
        </li>
        <li className="sidebar__list__item">
          <div className="avatar avatar--tiny">
            <span className="avatar__placeholder avatar__placeholder--lblue">С</span>
          </div>
        <span>
          Recent conversation 2
        </span>
        </li>
        <li className="sidebar__list__item sidebar__list__item--active2">
          <div className="avatar avatar--tiny">
            <span className="avatar__placeholder avatar__placeholder--red">W</span>
          </div>
        <span>
          Recent 3
        </span>
        </li>
      </ul>
    );
  }
});

module.exports = RecentSection;
