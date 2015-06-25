import React from 'react';

import AvatarItem from '../../common/AvatarItem.react';

class ContactItem extends React.Component {
  static propTypes = {
    contact: React.PropTypes.object,
    onToggle: React.PropTypes.func
  }

  constructor(props) {
    super(props);

    this.onToggle = this.onToggle.bind(this);
    this.state = {
      isSelected: false
    };
  }

  onToggle() {
    const isSelected = !this.state.isSelected;

    this.setState({
      isSelected: isSelected
    });

    this.props.onToggle(this.props.contact, isSelected);
  }

  render() {
    let contact = this.props.contact;

    let icon;

    if (this.state.isSelected) {
      icon = 'check_box';
    } else {
      icon = 'check_box_outline_blank';
    }

    return (
      <li className="contacts__list__item row">
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title">
            {contact.name}
          </span>
        </div>

        <div className="controls">
          <a className="material-icons" onClick={this.onToggle}>{icon}</a>
        </div>
      </li>
    );
  }
}

export default ContactItem;
