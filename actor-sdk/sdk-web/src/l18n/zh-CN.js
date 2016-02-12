export default {
  'locale': 'zh',
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
      'editProfile': '个人信息',
      'addToContacts': '添加联系人',
      'createGroup': '创建群',
      'configureIntegrations': '配置集成',
      'helpAndFeedback': 'Help & Feedback',
      'twitter': 'Our twitter',
      'preferences': '设置',
      'signOut': '登出'
    },

    // Buttons
    'button': {
      'ok': 'Ok',
      'cancel': 'Cancel',
      'done': 'Done',
      'requestCode': 'Request code',
      'checkCode': 'Check code',
      'signUp': 'Sign up',
      'add': '添加',
      'send': '发送',
      'sendAll': 'Send all',
      'createGroup': '创建群',
      'addMembers': '添加成员',
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
      }
    },

    // Compose
    'compose': {
      'attach': 'Attachment',
      'sendFile': '发送文件',
      'sendPhoto': '发送照片',
      'send': '发送',
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
        'notFound': 'Sorry, no users found.'
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
        'title': '添加联系人',
        'query': 'Email, nickname or phone',
        'phone': '手机号',
        'notFound': 'User with such data is not found',
        'empty': 'Start typing to search people',
        'searching': 'Search for "{query}"'
      },
      'createGroup': {
        'title': '创建群',
        'groupName': '群名称'
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
        'kick': 'Are you sure you want kick {name}'
      }
    },

    // Profiles
    'profile': {
      'email': 'email',
      'phone': 'phone',
      'nickname': 'nickname'
    },
    'createdBy': '创建者',
    'addPeople': '添加成员',
    'more': '更多',
    'actions': '操作',
    'addToContacts': '添加到联系人',
    'removeFromContacts': '从联系人中删除',
    'setGroupPhoto': '设置群头像',
    'addIntegration': '添加一个服务集成',
    'editGroup': '编辑群',
    'clearGroup': '清除群',
    'deleteGroup': '删除群',
    'clearConversation': '清除会话',
    'deleteConversation': '删除会话',
    'leaveGroup': '离开群',
    'sharedMedia': '已分享的媒体',
    'notifications': '通知',
    'integrationTokenCopied': '集成链接已复制',
    'members': '{numMembers, plural,' +
      '=0 {没有成员}' +
      '=1 {# 成员}' +
      'other {# 成员}' +
    '}',
    'kick': 'Kick',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does, this integration token allow the most flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Modals
    'inviteModalTitle': '添加更多成员',
    'inviteModalSearch': '搜索联系人或用户',
    'inviteModalNotFound': '抱歉，没有找到用户',
    'inviteByLink': '通过链接邀请至群',
    'inviteByLinkModalTitle': '通过链接邀请',
    'inviteByLinkModalDescription': '通过这个链接，所有人都可以进入群 ”{groupName}”:',
    'inviteByLinkModalCopyButton': '复制链接',
    'inviteByLinkModalRevokeButton': '撤销链接',
    'inviteLinkCopied': '邀请链接已复制',

    'preferencesModalTitle': '设置',
    'preferencesGeneralTab': '通用',
    'preferencesNotificationsTab': '通知 & 音效',
    'preferencesSecurityTab': '安全',
    'preferencesSendMessageTitle': '发送消息',
    'preferencesSendMessage': '发送消息',
    'preferencesNewLine': '换行',
    'preferencesEffectsTitle': '铃声',
    'preferencesEnableEffects': '启用铃声提醒',
    'preferencesNotificationsTitle': '通知',
    'preferencesNotificationsGroup': '启用群通知提醒',
    'preferencesNotificationsOnlyMention': '启用提到我的通知提醒',
    'preferencesNotificationsOnlyMentionHint': '当消息中提到我的时候才发送通知提醒',
    'preferencesPrivacyTitle': '隐私',
    'preferencesMessagePreview': '消息预览',
    'preferencesMessagePreviewHint': '从通知提醒中隐藏正文',
    'preferencesSessionsTitle': '已登陆的会话',
    'preferencesSessionsCurrentSession': '当前会话',
    'preferencesSessionsAuthTime': '登陆时间',
    'preferencesSessionsTerminate': '断开',
    'preferencesSessionsTerminateAll': '断开所有会话'
  }
};
