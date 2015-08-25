import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
import classnames from 'classnames';

import InviteUserStore from 'stores/InviteUserStore';

import { AsyncActionStates } from 'constants/ActorAppConstants';

import AvatarItem from 'components/common/AvatarItem.react';
import * as Stateful from 'components/common/Stateful.react';

const {addons: { PureRenderMixin }} = addons;

const getStateFromStore = (props) => {
  const { contact } = props;
  const group = InviteUserStore.getGroup();

  return {
    inviteUserState: InviteUserStore.getInviteUserState(group.id, contact.uid)
  }
};

@ReactMixin.decorate(PureRenderMixin)
class ContactItem extends React.Component {
  static propTypes = {
    contact: React.PropTypes.object,
    onSelect: React.PropTypes.func,
    member: React.PropTypes.bool
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStore(props);
  }

  componentWillUnmount() {
    const { contact } = this.props;
    const group = InviteUserStore.getGroup();
    InviteUserStore.resetInviteUserState(group.id, contact.uid);
  }

  onSelect = () => {
    const { contact } = this.props;

    this.props.onSelect(contact);

    InviteUserStore.addChangeListener(this.onChange);
    this.setState({inviteUserState: AsyncActionStates.PROCESSING}); // Used for immediately set processing state
  };

  onChange = () => {
    this.setState(getStateFromStore(this.props));

    setTimeout(() => {
      const { inviteUserState } = this.state;
      if (inviteUserState === AsyncActionStates.SUCCESS || inviteUserState === AsyncActionStates.FAILURE) {
        InviteUserStore.removeChangeListener(this.onChange);
      }
    }, 0);
  };

  render() {
    const { contact, member } = this.props;
    const { inviteUserState } = this.state;

    const contactClassName = classnames('contacts__list__item row', {
      'contacts__list__item--member': inviteUserState === AsyncActionStates.SUCCESS || member
    });

    const controls = member
      ? <i className="material-icons">check</i>
      : <Stateful.Root currentState={inviteUserState}>
          <Stateful.Pending>
            <a className="material-icons" onClick={this.onSelect}>person_add</a>
          </Stateful.Pending>
          <Stateful.Processing>
            <i className="material-icons spin">autorenew</i>
          </Stateful.Processing>
          <Stateful.Success>
            <i className="material-icons">check</i>
          </Stateful.Success>
          <Stateful.Failure>
            <i className="material-icons">warning</i>
          </Stateful.Failure>
        </Stateful.Root>;

    return (
      <li className={contactClassName}>
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
          {controls}
        </div>
      </li>
    );
  }
}

export default ContactItem;
