/*
 * Copyright (C) 2015 Actor LLC. <https://actor.im>
 */

import React from 'react';

import { escapeWithEmoji } from 'utils/EmojiUtils';

import AvatarItem from 'components/common/AvatarItem.react';

class ContactItem extends React.Component {
  static propTypes = {
    contact: React.PropTypes.object,
    onToggle: React.PropTypes.func
  };

  constructor(props) {
    super(props);

    this.state = {
      isSelected: false
    };
  }

  onToggle = () => {
    const { contact, onToggle } = this.props;
    const { isSelected } = this.state;

    this.setState({isSelected: !isSelected});

    onToggle(contact, !isSelected);
  };

  render() {
    const { contact } = this.props;
    const { isSelected } = this.state;

    const icon = (isSelected) ? 'check_box' : 'check_box_outline_blank';

    return (
      <li className="contacts__list__item row">
        <AvatarItem image={contact.avatar}
                    placeholder={contact.placeholder}
                    size="small"
                    title={contact.name}/>

        <div className="col-xs">
          <span className="title" dangerouslySetInnerHTML={{__html: escapeWithEmoji(contact.name)}}/>
        </div>

        <div className="controls">
          <a className="material-icons" onClick={this.onToggle}>{icon}</a>
        </div>
      </li>
    );
  }
}

export default ContactItem;
