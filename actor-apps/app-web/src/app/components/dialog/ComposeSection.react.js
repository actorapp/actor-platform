import _ from 'lodash';

import React from 'react';
import ReactMixin from 'react-mixin';
import addons from 'react/addons';
const {addons: { PureRenderMixin }} = addons;

import ActorClient from 'utils/ActorClient';
import Inputs from 'utils/Inputs';
import { Styles, FlatButton } from 'material-ui';

import { KeyCodes } from 'constants/ActorAppConstants';
import ActorTheme from 'constants/ActorTheme';

import MessageActionCreators from 'actions/MessageActionCreators';
import ComposeActionCreators from 'actions/ComposeActionCreators';

import GroupStore from 'stores/GroupStore';
import PreferencesStore from 'stores/PreferencesStore';
import ComposeStore from 'stores/ComposeStore';

import AvatarItem from 'components/common/AvatarItem.react';
import { Dropdown, DropdownItem } from 'components/common/Dropdown.react';

const ThemeManager = new Styles.ThemeManager();

let getStateFromStores = () => {
  return {
    text: ComposeStore.getText(),
    profile: ActorClient.getUser(ActorClient.getUid()),
    sendByEnter: PreferencesStore.sendByEnter,
    mentions: ComposeStore.getMentions()
  };
};

@ReactMixin.decorate(PureRenderMixin)
class ComposeSection extends React.Component {
  static propTypes = {
    peer: React.PropTypes.object.isRequired
  };

  static childContextTypes = {
    muiTheme: React.PropTypes.object
  };

  constructor(props) {
    super(props);

    this.state = getStateFromStores();

    ThemeManager.setTheme(ActorTheme);
    GroupStore.addChangeListener(this.onChange);
    ComposeStore.addChangeListener(this.onChange);
    PreferencesStore.addChangeListener(this.onChange);
  }

  componentWillUnmount() {
    GroupStore.removeChangeListener(this.onChange);
    GroupStore.removeChangeListener(getStateFromStores);
    PreferencesStore.addChangeListener(this.onChange);
  }

  onChange = () => {
    this.setState(getStateFromStores());
  };

  getChildContext() {
    return {
      muiTheme: ThemeManager.getCurrentTheme()
    };
  }

  onMessageChange = event => {
    let text = event.target.value;

    ComposeActionCreators.onTyping(this.props.peer, text, this.getCaretPosition());
  };

  onKeyDown = event => {
    if (this.state.mentions === null) {
      if (this.state.sendByEnter === 'true') {
        if (event.keyCode === KeyCodes.ENTER && !event.shiftKey) {
          event.preventDefault();
          this.sendTextMessage();
        }
      } else {
        if (event.keyCode === KeyCodes.ENTER && event.metaKey) {
          event.preventDefault();
          this.sendTextMessage();
        }
      }
    }
  };

  sendTextMessage = () => {
    const text = this.state.text;
    if (text) {
      MessageActionCreators.sendTextMessage(this.props.peer, text);
    }
    ComposeActionCreators.cleanText();
  };

  onSendFileClick = () => {
    const fileInput = document.getElementById('composeFileInput');
    fileInput.click();
  };

  onSendPhotoClick = () => {
    const photoInput = document.getElementById('composePhotoInput');
    photoInput.accept = 'image/*';
    photoInput.click();
  };

  onFileInputChange = () => {
    const files = document.getElementById('composeFileInput').files;
    MessageActionCreators.sendFileMessage(this.props.peer, files[0]);
  };

  onPhotoInputChange = () => {
    const photos = document.getElementById('composePhotoInput').files;
    MessageActionCreators.sendPhotoMessage(this.props.peer, photos[0]);
  };

  onPaste = event => {
    let preventDefault = false;

    _.forEach(event.clipboardData.items, (item) => {
      if (item.type.indexOf('image') !== -1) {
        preventDefault = true;
        MessageActionCreators.sendClipboardPhotoMessage(this.props.peer, item.getAsFile());
      }
    }, this);

    if (preventDefault) {
      event.preventDefault();
    }
  };

  onMentionSelect = (value) => {
    ComposeActionCreators.insertMention(this.props.peer, this.state.text, this.getCaretPosition(), value);
    this.refs.area.getDOMNode().focus();
  };

  onMentionClose = () => {
    ComposeActionCreators.closeMention();
  };

  getCaretPosition = () => {
    let el = this.refs.area.getDOMNode();
    let selection = Inputs.getInputSelection(el);
    return selection.start;
  };

  render() {
    const text = this.state.text;
    const profile = this.state.profile;

    const mentionsShown = this.state.mentions && this.state.mentions.length > 0;

    let mentionsElements = null;

    if (mentionsShown) {
      mentionsElements = _.map(this.state.mentions, (mention) => {
        return (
          <DropdownItem value={mention.title}>
            <AvatarItem image={mention.avatar}
                        placeholder={mention.placeholder}
                        size="tiny"
                        title={mention.title}/>
            {mention.title}
          </DropdownItem>
        );
      });
    }

    return (
      <section className="compose" onPaste={this.onPaste}>

        <Dropdown className="dropdown--mentions"
                  onSelect={this.onMentionSelect}
                  onClose={this.onMentionClose}
                  isShown={mentionsShown}
                  ref="mentions">
          {mentionsElements}
        </Dropdown>

        <AvatarItem className="my-avatar"
                    image={profile.avatar}
                    placeholder={profile.placeholder}
                    title={profile.name}/>


          <textarea className="compose__message"
                    onChange={this.onMessageChange}
                    onKeyDown={this.onKeyDown}
                    value={text}
                    ref="area">
          </textarea>

        <footer className="compose__footer row">
          <button className="button" onClick={this.onSendFileClick}>
            <i className="material-icons">attachment</i> Send file
          </button>
          <button className="button" onClick={this.onSendPhotoClick}>
            <i className="material-icons">photo_camera</i> Send photo
          </button>

          <span className="col-xs"></span>

          <FlatButton label="Send" onClick={this.sendTextMessage} secondary={true}/>
        </footer>

        <div className="compose__hidden">
          <input id="composeFileInput"
                 onChange={this.onFileInputChange}
                 type="file"/>
          <input id="composePhotoInput"
                 onChange={this.onPhotoInputChange}
                 type="file"/>
        </div>
      </section>
    );
  }
}

export default ComposeSection;
