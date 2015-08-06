/* eslint-disable */
import React from 'react';
import classnames from 'classnames';

class Fold extends React.Component {
  static PropTypes = {
    icon: React.PropTypes.string,
    iconClassName: React.PropTypes.string,
    title: React.PropTypes.string.isRequired
  };

  state = {
    isOpen: false
  };

  constructor(props) {
    super(props);
  }

  render() {
    const { icon, iconClassName, title, iconElement } = this.props;
    const titleIconClassName = classnames('material-icons icon', iconClassName);
    const className = classnames({
      'fold': true,
      'fold--open': this.state.isOpen
    });

    let foldIcon;
    if (icon) {
      foldIcon = <i className={titleIconClassName}>{icon}</i>;
    }
    if (iconElement) {
      foldIcon = iconElement;
    }

    return (
      <div className={className}>
        <div className="fold__title" onClick={this.onClick}>
          {foldIcon}
          {title}
          <i className="fold__indicator material-icons pull-right">arrow_drop_down</i>
        </div>
        <div className="fold__content">
          {this.props.children}
        </div>
      </div>
    );
  }

  onClick = () => {
    this.setState({isOpen: !this.state.isOpen});
  };
}

export default Fold;
