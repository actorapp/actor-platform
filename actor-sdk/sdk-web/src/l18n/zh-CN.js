export default {
  'locale': 'zh',
  'messages': {
      // Login
      'login': {
          'signIn': '登录',
          'wrong': 'Wrong?',
          'phone': '手机号码',
          'email': '邮箱地址',
          'phone_or_email': 'Phone number or Email address',
          'username':'用户名',
          'authCode': 'Auth code',
          'password':'密码',
          'yourName': 'Your name',
          'errors': {
              'numberInvalid': 'Invalid number',
              'nameInvalid': 'Invalid name',
              'codeInvalid': 'Invalid code',
              'codeExpired': 'Phone code is expired',
              'codeWait': 'Try to request code later',
              'passwordInvalid':'密码错误'
          },
          'welcome': {
              'header': '欢迎使用<strong>易联</strong>',
              'text': '<p>{appName} Messenger brings all your business network connections into one place, makes it easily accessible wherever you go.</p>' +
              '<p>Our aim is to make your work easier, reduce your email amount, make the business world closer by reducing time to find right contacts.</p>',
              'copyright': '{appName} Messenger  2015'
          }
      },
    // Menus
    'menu': {
      // Sidebar menu
      'editProfile': '个人信息',
      'addToContacts': '添加联系人',
      'createGroup': '创建群',
      'configureIntegrations': '配置集成',
      'preferences': '设置',
      'signOut': '登出',
      'homePage': '开发公司主页'
    },

    // Buttons
    'button': {
      'add': '添加',
      'send': '发送',
      'createGroup': '创建群',
      'addMembers': '添加成员',
      'quickSearch': '通讯录',
      'checkCode': '提交',
      'requestCode': '检查用户名',
      'call': '通话'

    },

    // Compose
    'compose': {
      'sendFile': '发送文件',
      'sendPhoto': '发送照片',
      'send': '发送'
    },

    // Modals
    'modal': {
      'profile': {
          'title': '个人简介',
          'name': '全名',
          'nick': '昵称',
          'phone': '电话号码',
          'email': '邮箱',
          'about': '关于',
          'avatarChange': '修改头像',
          'avatarRemove': '删除头像'
      },
      'group': {
          'title': '建群',
          'name': '群组主题',
          'about': '群组介绍',
          'avatarChange': '修改群组头像',
          'avatarRemove': '删除'
      },
      'groups': {
          'title': '群组',
          'search': '群找群组',
          'loading': '加载',
          'notFound': '没有找到 <strong>{query}</strong>群组.'
      },

      'addContact': {
        'title': '添加联系人',
        'phone': '手机号'
      },
      'createGroup': {
        'title': '创建群',
        'groupName': '群名称'
      }
    },

    // Profiles
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
    'preferencesSessionsTerminateAll': '断开所有会话',

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
              'addPeople': 'add people',
              'favourites': 'Favorites',
              'groups': '群组',
              'privates': '最近联系人',
          },
          'group': {
              'empty': 'Create your first group conversation'
          },
          'private': {
              'empty': 'There is no one in your network'
          }
    },
    'main': {
          'empty': 'Try to be better than yesterday!',
          'install': '<h1>Web version of <b>{appName}</b> works only on desktop browsers at this time</h1>' +
          '<h3>Please install our apps for using <b>{appName}</b> on your phone.</h3>' +
          '<p><a href="//actor.im/ios">iPhone</a> | <a href="//actor.im/android">Android</a></p>',
          'deactivated': {
              'header': 'Tab deactivated',
              'text': 'Oops, we have detected another tab with {appName}, so we had to deactivate this one to prevent some dangerous things happening.'
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
  video: 'Video'
  },

  toolbar: {
  callState: {
  calling: 'calling',
  connecting: 'connecting',
  in_progress: 'On call: {time}',
  ended: 'ended'
  }
  }

  }
};
