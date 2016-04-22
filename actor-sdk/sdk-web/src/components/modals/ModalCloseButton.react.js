import React, { Component, PropTypes } from 'react';
import { FormattedMessage } from 'react-intl';

class ModalCloseButton extends Component {
  static propTypes = {
    onClick: PropTypes.func.isRequired
  }

  constructor(props) {
    super(props);

    this.handleClick = this.handleClick.bind(this);
  }

  handleClick() {
    this.props.onClick();
  }

  render() {
    return (
      <div className="modal__close-button" onClick={this.handleClick}>
        <i className="close_icon material-icons">close</i>
        <div className="text"><FormattedMessage id="button.close"/></div>
      </div>
    )
  }
}

export default ModalCloseButton;
