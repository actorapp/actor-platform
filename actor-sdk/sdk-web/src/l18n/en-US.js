export default {
  'locale': 'en',
  'messages': {
    // Login
    'login': {
      'signIn': 'Sign in',
      'wrong': 'Wrong?',
      'phone': 'Phone number',
      'email': 'Email address',
      'phone_or_email': 'Phone number or Email address',
      'authCode': 'Auth code',
      'yourName': 'Your name',
      'errors': {
        'numberInvalid': 'Invalid number',
        'nameInvalid': 'Invalid name',
        'codeInvalid': 'Invalid code',
        'codeExpired': 'Phone code has expired',
        'codeWait': 'Try to request code later'
      },
      'welcome': {
        'header': 'Welcome to <strong>{appName}</strong>',
        'text': '<p>{appName} Messenger brings all your business network connections into one place.</p>' +
                '<p>Our goal is making your working process easier, reducing your email load and making the people in business world closer to each other.</p>',
        'copyright': '{appName} Messenger © 2016'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      editProfile: 'Edit profile',
      addToContacts: 'Add contact',
      createGroup: 'Create group',
      blockedUsers: 'Blocked users',
      helpAndFeedback: 'Help & Feedback',
      twitter: 'Our Twitter',
      preferences: 'Preferences',
      signOut: 'Sign Out',
      homePage: 'Home page',
      facebook: 'Like us on Facebook'
    },

    // Buttons
    'button': {
      'ok': 'Ok',
      'cancel': 'Cancel',
      'done': 'Done',
      'requestCode': 'Request code',
      'checkCode': 'Check code',
      'signUp': 'Sign up',
      'add': 'Add',
      'send': 'Send',
      'sendAll': 'Send all',
      'createGroup': 'Create group',
      'addMembers': 'Add users',
      'quickSearch': 'Quick search',
      'close': 'Close',
      'save': 'Save',
      'call': 'Make call',
      'archive': 'Archive',
      'invite': '+ Invite people'
    },

    // Messages
    'message': {
      'pin': 'Pin',
      'reply': 'Reply',
      'forward': 'Forward',
      'download': 'Download',
      'delete': 'Delete',
      'quote': 'Quote',
      'uploading': 'Uploading...',
      'welcome': {
        'private': '<p>This is the very beginning of your private conversation with <strong>{name}</strong>.</p><p>All messages here are only visible to the two of you.</p>',
        'group': {
          'main': '<p>This is the very beginning of <strong>{name}</strong> group conversation created by {creator}.</p>',
          'you': 'You',
          'actions': {
            'start': 'You can ',
            'end': ' to this conversation.',
            'invite': 'invite more users'
          }
        }
      },
      'loading': 'Loading messages from history',
      'unread': 'New messages'
    },

    // Connection state
    'connectionState': {
      'connecting': 'Houston, we have a problem! Connection to {appName} server is lost. Trying to reconnect now...',
      'online': 'You\'re back online!',
      'updating': 'Update connection'
    },

    // Compose
    'compose': {
      'attach': 'Attachment',
      'sendFile': 'Send file',
      'sendPhoto': 'Send photo',
      'send': 'Send',
      'edit': 'Save',
      'cancel': 'Cancel',
      'editTitle': 'Edit message:',
      'markdown': {
        'bold': 'bold',
        'italic': 'italics',
        'preformatted': 'preformatted'
      },
      'dropzone': 'Drop here to send.',
      'notMember': 'You are not a member of this group',
      'start': 'Start',
      'unblock': 'Unblock user'
    },

    // Modals
    'modal': {
      'profile': {
        'title': 'Profile',
        'name': 'Name',
        'nick': 'Nickname',
        'phone': 'Phone number',
        'email': 'Email',
        'about': 'About',
        'avatarChange': 'Change avatar',
        'avatarRemove': 'Remove',
        errors: {
          nick: {
            length: 'Nickname should be longer than 5 and shorter than 32 characters',
            chars: 'Please use latin characters, numbers and underscores'
          }
        }
      },
      'group': {
        'title': 'Edit group',
        'name': 'Group title',
        'about': 'Group summary',
        'avatarChange': 'Change avatar',
        'avatarRemove': 'Remove'
      },
      'crop': {
        'title': 'Crop picture'
      },
      'contacts': {
        'title': 'People',
        'search': 'Search contacts',
        'notFound': 'Sorry, no such users were found :(',
        'loading': 'Loading'
      },
      'groups': {
        'title': 'Groups',
        'search': 'Search',
        'loading': 'Loading',
        'notFound': 'No match found for <strong>{query}</strong>.'
      },
      'attachments': {
        'title': 'Send file',
        'name': 'Filename',
        'type': 'Type',
        'size': 'Size',
        'extra': 'Extra',
        'sendAsPicture': 'Send as picture'
      },
      'addContact': {
        'title': 'Add contact',
        'query': 'Email, nickname or phone number',
        'phone': 'Phone number',
        'notFound': 'User with such data is not found',
        'empty': 'Start typing to find people',
        'searching': 'Search for "{query}"'
      },
      'createGroup': {
        'title': 'Create group',
        'groupName': 'Group name'
      },
      'quickSearch': {
        'title': 'Search everywhere',
        'placeholder': 'Start typing',
        'toNavigate': 'to navigate',
        'toSelect': 'to select',
        'toClose': 'to close',
        'openDialog': 'Open conversation',
        'startDialog': 'Start a new conversation',
        'notFound': 'No matches found for <strong>{query}</strong>.<br/>Did you spell it correctly?'
      },
      'confirm': {
        'logout': 'Are you sure you want to leave?',
        'user': {
          'clear': 'Are you sure you want to clear the conversation with <strong>{name}</strong>?',
          'delete': 'Are you sure you want to delete the conversation with <strong>{name}</strong>?',
          'block': 'Are you sure you want to block <strong>{name}</strong>?',
          'removeContact': 'Are you sure you want to remove <strong>{name}</strong> from your contacts?'
        },
        'group': {
          'clear': 'Are you sure you want to clear the conversation <strong>{name}</strong>?',
          'delete': 'Are you sure you want to delete the conversation <strong>{name}</strong>?',
          'leave': 'Are you sure you want to leave the conversation <strong>{name}</strong>?',
          'kick': 'Are you sure you want to kick <strong>{name}</strong>?'
        },
        'nonContactHide': {
          'title': 'Are you sure you want to hide this conversation?',
          'body': 'User {name} isn\'t in your contact list.'
        },
        delete: 'Delete this group?',
        kick: 'Kick this user?'
      }
    },

    // Profiles
    'profile': {
      'email': 'email',
      'phone': 'phone',
      'nickname': 'nickname',
      'about': 'about'
    },
    'createdBy': 'сreated by',
    'addPeople': 'Add people',
    'more': 'More',
    'actions': 'Actions',
    'addToContacts': 'Add to contacts',
    'removeFromContacts': 'Remove from contacts',
    'setGroupPhoto': 'Set group photo',
    'addIntegration': 'Add a Service integration',
    'editGroup': 'Edit group',
    'clearGroup': 'Clear group',
    'deleteGroup': 'Delete group',
    'clearConversation': 'Clear conversation',
    'deleteConversation': 'Delete conversation',
    'blockUser': 'Block user',
    'unblockUser': 'Unblock user',
    'leaveGroup': 'Leave group',
    'sharedMedia': 'Shared media',
    'notifications': 'Notifications',
    'integrationTokenCopied': 'Integration link copied.',
    'members': '{numMembers, plural,' +
      '=0 {No members}' +
      '=1 {# Member}' +
      'other {# Members}' +
    '}',
    'kick': 'Kick',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does — this integration token allows the biggest amount of flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Sidebar
    'sidebar': {
      'recents': {
        'empty': {
          'first': 'You don\'t have any ongoing conversations at the moment.',
          'second': {
            'start': 'You can ',
            'or': ' or ',
            'end': '.'
          }
        },
        'newDialog': 'create new dialogue',
        'addPeople': 'add people',
        'favourites': 'Favorites',
        'groups': 'Groups',
        'privates': 'Direct Messages',
        'history': 'History'
      },
      'group': {
        'empty': 'Create your first group conversation'
      },
      'private': {
        'empty': 'There is no one in your network yet'
      }
    },

    'main': {
      'empty': 'Try to be better than yesterday!',
      'install': '<h1>The Web version of <b>{appName}</b> only works in desktop browsers at this time</h1>' +
                 '<h3>Try our apps for using <b>{appName}</b> on your phone!</h3>' +
                 '<p><a href="//actor.im/ios">iPhone</a> | <a href="//actor.im/android">Android</a></p>',
      'deactivated': {
        'header': 'Tab deactivated',
        'text': 'Oops, we have detected another tab running {appName}! We had to deactivate it to prevent you from dangerous things happening.'
      }
    },

    preferences: {
      title: 'Preferences',
      general: {
        title: 'General',
        send: {
          title: 'Send message',
          sendMessage: 'send message',
          newLine: 'new line'
        }
      },
      interface: {
        title: 'Interface',
        animation: {
          title: 'Autoplay animations'
        }
      },
      notifications: {
        title: 'Notifications & Sounds',
        effects: {
          title: 'Effects',
          enable: 'Enable sound effects'
        },
        notification: {
          title: 'Notifications',
          enable: 'Enable group notifications',
          onlyMentionEnable: 'Enable mention only notifications',
          onlyMentionHint: 'You can enable notifications only for messages that contains you mention.'
        },
        privacy: {
          title: 'Privacy',
          messagePreview: 'Message preview',
          messagePreviewHint: 'Remove message text from notifications.'
        }
      },
      security: {
        title: 'Security',
        sessions: {
          title: 'Active sessions',
          current: 'Current session',
          authTime: 'Auth time',
          terminate: 'Kill',
          terminateAll: 'Terminate all sessions'
        }
      },
      blocked: {
        title: 'Blocked Users',
        notExists: 'You haven\'t blocked anyone yet.',
        notFound: 'Sorry, no such users were found.',
        search: 'Search for contacts or usernames',
        unblock: 'Unblock'
      },
      about: {
        title: 'About'
      }
    },

    invite: {
      title: 'Add more people',
      search: 'Search for contacts or usernames',
      notFound: 'Sorry, no such users were found.',
      inviteByLink: 'Invite to group by link',
      byLink: {
        title: 'Invite by link',
        description: 'Anyone on the web will be able to join ”<b>{groupName}</b>” by opening this link:',
        copy: 'Copy link',
        revoke: 'Revoke link'
      }
    },

    call: {
      outgoing: 'Outgoing call',
      incoming: 'Incoming call',
      mute: 'Mute',
      unmute: 'Unmute',
      answer: 'Answer',
      decline: 'Decline',
      end: 'End call',
      addUser: 'Add user',
      fullScreen: 'Fullscreen',
      video: 'Video',
      state: {
        calling: 'calling',
        connecting: 'connecting',
        in_progress: 'On call: {time}',
        ended: 'ended'
      }
    },

    tooltip: {
      toolbar: {
        info: 'Information about the current conversation',
        favorite: 'Toggle favorite'
      },
      recent: {
        groupsList: 'Group conversation list',
        privatesList: 'Private conversation list',
        groupsCreate: 'Create group',
        privatesCreate: 'Add new contact'
      },
      quicksearch: 'The fastest way to find something'
    },

    context: {
      favorite: {
        add: 'Favorite',
        remove: 'Unfavorite'
      },
      archive: 'Send to archive',
      delete: 'Delete'
    },

    search: {
      'placeholder': 'Search',
      'emptyQuery': 'Start typing to find anything',
      'searching': 'Search for "{query}"',
      'notFound': 'Nothing found for "{query}"<br/>Maybe you\'ll get luckier with another request',
      hint: '<h4>Fast search</h4><p>You can search for contacts, dialogs, or messages from the current dialog here.</p>',
      inDialog: 'Search messages in current dialog'
    }
  }
};
