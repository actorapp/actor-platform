/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';

const {addons: { PureRenderMixin }} = addons;

class ActivityHeader extends React.Component {
  static propTypes = {
    close: React.PropTypes.func,
    title: React.PropTypes.string
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { title, close } = this.props;

    let headerTitle;
    if (typeof title !== 'undefined') {
      headerTitle = <span className="activity__header__title">{title}</span>;
    }

    return (
      <header className="activity__header toolbar">
        <a className="activity__header__close material-icons" onClick={close}>clear</a>
        {headerTitle}
      </header>
    );
  }
}

ReactMixin.onClass(ActivityHeader, PureRenderMixin);

export default ActivityHeader;
