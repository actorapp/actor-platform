export default {
  'locales': 'en-US',
  'messages': {
    // Login
    'login': {
      'signIn': 'Sign in',
      'wrong': 'Wrong?',
      'phone': 'Phone number',
      'email': 'Email address',
      'authCode': 'Auth code',
      'yourName': 'Your name',
      'errors': {
        'numberInvalid': 'Invalid number',
        'nameInvalid': 'Invalid name',
        'codeInvalid': 'Invalid code',
        'codeExpired': 'Phone code is expired',
        'codeWait': 'Try to request code later'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      'editProfile': 'Edit Profile',
      'addToContacts': 'Add contact',
      'createGroup': 'Create group',
      'configureIntegrations': 'Configure Integrations',
      'helpAndFeedback': 'Help & Feedback',
      'twitter': 'Our twitter',
      'preferences': 'Preferences',
      'signOut': 'Sign Out'
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
      'close': 'Close'
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
        'private': '<p>This is the very beginning of your private conversation with <strong>{name}</strong>.</p><p>All messages here are private between the two of you.</p>',
        'group': {
          'main': '<p>This is the very beginning of <strong>{name}</strong> group conversation which created by {creator}.</p>',
          'you': 'You',
          'actions': {
            'start': 'You can ',
            'end': ' to this conversation.',
            'invite': 'invite more users'
          }
        }
      },
      'loading': 'Loading messages from history'
    },

    // Connection state
    'connectionState': {
      'connecting': 'Houston, we have a problem! Connection to Actor server is lost. Trying to reconnect now...',
      'online': 'You\'re back online!',
      'updating': ''
    },

    // Compose
    'compose': {
      'attach': 'Attachment',
      'sendFile': 'Send file',
      'sendPhoto': 'Send photo',
      'send': 'Send',
      'markdown': {
        'bold': 'bold',
        'italic': 'italics',
        'preformatted': 'preformatted'
      },
      'dropzone': 'Drop here to send.'
    },

    // Modals
    'modal': {
      'profile': {
        'title': 'Profile',
        'name': 'Full name',
        'nick': 'Nickname',
        'phone': 'Phone number',
        'email': 'Email',
        'about': 'About',
        'avatarChange': 'Change avatar',
        'avatarRemove': 'Remove'
      },
      'group': {
        'title': 'Edit group',
        'name': 'Group title',
        'about': 'Group about',
        'avatarChange': 'Change avatar',
        'avatarRemove': 'Remove'
      },
      'crop': {
        'title': 'Crop picture'
      },
      'contacts': {
        'title': 'People',
        'search': 'Search contacts',
        'notFound': 'Sorry, no users found.',
        'loading': 'Loading'
      },
      'groups': {
        'title': 'Groups',
        'search': 'Search',
        'loading': 'Loading',
        'notFound': 'No matches found for <strong>{query}</strong>.'
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
        'query': 'Email, nickname or phone',
        'phone': 'Phone number',
        'notFound': 'User with such data is not found',
        'empty': 'Start typing to search people',
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
        'startDialog': 'Start new conversation',
        'notFound': 'No matches found for <strong>{query}</strong>.<br/>Have you spelled it correctly?'
      },
      'confirm': {
        'logout': 'Do you really want to leave?',
        'leave': 'Do you really want to leave this conversation?',
        'clear': 'Do you really want to clear this conversation?',
        'delete': 'Do you really want to delete this conversation?',
        'removeContact': 'You really want to remove {name} from your contacts?',
        'kick': 'Are you sure you want kick {name}',
        'nonContactHide': {
          'title': 'Are you sure you want to hide this conversation?',
          'body': 'User {name} isn\'t in your contact list.'
        }
      }
    },

    // Profiles
    'profile': {
      'email': 'email',
      'phone': 'phone',
      'nickname': 'nickname'
    },
    'createdBy': 'сreated by',
    'addPeople': 'Add people',
    'more': 'More',
    'actions': 'Actions',
    'addToContacts': 'Add To Contacts',
    'removeFromContacts': 'Remove From Contacts',
    'setGroupPhoto': 'Set Group Photo',
    'addIntegration': 'Add a Service Integration',
    'editGroup': 'Edit Group',
    'clearGroup': 'Clear Group',
    'deleteGroup': 'Delete Group',
    'clearConversation': 'Clear Conversation',
    'deleteConversation': 'Delete Conversation',
    'leaveGroup': 'Leave Group',
    'sharedMedia': 'Shared Media',
    'notifications': 'Notifications',
    'integrationTokenCopied': 'Integration link copied.',
    'members': '{numMembers, plural,' +
      '=0 {No members}' +
      '=1 {# Member}' +
      'other {# Members}' +
    '}',
    'kick': 'Kick',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does, this integration token allow the most flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Modals
    'inviteModalTitle': 'Add More People',
    'inviteModalSearch': 'Search for contacts or usernames',
    'inviteModalNotFound': 'Sorry, no users found.',
    'inviteByLink': 'Invite to group by link',
    'inviteByLinkModalTitle': 'Invite by link',
    'inviteByLinkModalDescription': 'Anyone on the web will be able to join ”{groupName}” by opening this link:',
    'inviteByLinkModalCopyButton': 'Copy link',
    'inviteByLinkModalRevokeButton': 'Revoke link',
    'inviteLinkCopied': 'Invitation link copied.',

    'preferencesModalTitle': 'Preferences',
    'preferencesGeneralTab': 'General',
    'preferencesNotificationsTab': 'Notifications & Sounds',
    'preferencesSecurityTab': 'Security',
    'preferencesSendMessageTitle': 'Send Message',
    'preferencesSendMessage': 'send message',
    'preferencesNewLine': 'new line',
    'preferencesEffectsTitle': 'Effects',
    'preferencesEnableEffects': 'Enable sound effects',
    'preferencesNotificationsTitle': 'Notifications',
    'preferencesNotificationsGroup': 'Enable group notifications',
    'preferencesNotificationsOnlyMention': 'Enable mention only notifications',
    'preferencesNotificationsOnlyMentionHint': 'You can enable notifications only for messages that contains you mention.',
    'preferencesPrivacyTitle': 'Privacy',
    'preferencesMessagePreview': 'Message preview',
    'preferencesMessagePreviewHint': 'Remove message text from notifications.',
    'preferencesSessionsTitle': 'Active sessions',
    'preferencesSessionsCurrentSession': 'Current session',
    'preferencesSessionsAuthTime': 'Auth time',
    'preferencesSessionsTerminate': 'Kill',
    'preferencesSessionsTerminateAll': 'Terminate all sessions',

    // Sidebar
    'sidebar': {
      'recents': {
        'empty': {
          'first': 'You don\'t have any conversations at this moment.',
          'second': {
            'start': 'You can ',
            'or': ' or ',
            'end': '.'
          }
        },
        'newDialog': 'create new dialog',
        'addPeople': 'add people'
      }
    }
  }
};
