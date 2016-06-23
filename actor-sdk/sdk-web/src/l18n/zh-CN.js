export default {
  'locale': 'zh',
  'messages': {
    // Login
    'login': {
      'signIn': '登陆',
      'wrong': '切换',
      'phone': '手机号码',
      'email': 'mail地址',
      'user': '账号',
      'phone_or_email': '电话号码或者EMAIL地址',
      'authCode': '验证码',
      'authPassword': '密码',
      'yourName': '您的名字',
      'errors': {
        'numberInvalid': '号码错误',
        'nameInvalid': '用户错误',
        'codeInvalid': '验证码错误',
        'passwordInvalid': '密码错误',
        'codeExpired': '验证码过期',
        'codeWait': '稍后重试'
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
      editProfile: '个人信息',
      addToContacts: '添加联系人',
      createGroup: '创建群组',
      blockedUsers: '屏蔽用户',
      helpAndFeedback: '用户帮助与反馈',
      twitter: 'Our Twitter',
      preferences: '设置',
      signOut: '更换用户',
      homePage: '公司主页',
      facebook: 'Like us on Facebook'
    },

    // Buttons
    'button': {
      'ok': '确定',
      'cancel': '取消',
      'done': '完成',
      'requestCode': '发送验证码',
      'validateUsername': '验证账号',
      'checkCode': '验证',
      'checkPassword': '验证密码',
      'signUp': '注册',
      'add': '添加',
      'send': '发送',
      'sendAll': '全部发送',
      'createGroup': '创建群组',
      'addMembers': '添加成员',
      'quickSearch': '通讯录',
      'close': '关闭',
      'save': '保存',
      'call': '电话',
      'archive': '过往联络人',
      'invite': '+ 邀请'
    },

    // Messages
    'message': {
      'pin': 'Pin',
      'reply': '回复',
      'forward': '前进',
      'download': '下载',
      'delete': '删除',
      'quote': '引用',
      'uploading': '上传中...',
      'welcome': {
        'private': '<p>您和 <strong>{name}开始聊天</strong>.</p><p>所有的消息都经过加密,只有参与聊天的账号才可以查看.</p>',
        'group': {
          'main': '<p>群组 <strong>{name}</strong> 由 {creator}创建.</p>',
          'you': '您',
          'actions': {
            'start': '您可以 ',
            'end': ' 到这个聊天群组.',
            'invite': '邀请更多人'
          }
        }
      },
      'loading': '加载更多更早的聊天记录',
      'unread': '新的消息'
    },

    // Connection state
    'connectionState': {
      'connecting': '连接服务器失败,正在尝试重连...',
      'online': '连接服务器成功!',
      'updating': '更新连接状态'
    },

    // Compose
    'compose': {
      'attach': '附件',
      'sendFile': '发送文件',
      'sendPhoto': '发送图片',
      'send': '发送',
      'edit': '保存',
      'cancel': '取消',
      'editTitle': '编辑消息:',
      'markdown': {
        'bold': '加粗',
        'italic': '斜体',
        'preformatted': 'preformatted'
      },
      'dropzone': '拖拽到此处发送.',
      'notMember': '您不是该群组的成员',
      'start': '开始',
      'unblock': '屏蔽用户'
    },

    // Modals
    'modal': {
      'profile': {
        'title': '个人信息',
        'name': '昵称',
        'nick': '账号',
        'phone': '手机号',
        'email': '邮箱',
        'about': '关于',
        'avatarChange': '修改头像',
        'avatarRemove': '删除头像',
        errors: {
          nick: {
            length: '昵称长度限制为5至32',
            chars: 'Please use latin characters, numbers and underscores'
          }
        }
      },
      'group': {
        'title': '编辑群组',
        'name': '群组名称',
        'about': '群组描述',
        'avatarChange': '修改群头像',
        'avatarRemove': '删除群头像'
      },
      'crop': {
        'title': 'Crop picture'
      },
      'contacts': {
        'title': '联系人',
        'search': '搜索',
        'notFound': '没有找到指定联系人 :(',
        'loading': '加载中'
      },
      'groups': {
        'title': '群组',
        'search': '搜索',
        'loading': '加载中',
        'notFound': '没有找到与 <strong>{query}</strong>匹配的联系人.'
      },
      'attachments': {
        'title': '文件',
        'name': '文件名',
        'type': '类型',
        'size': '大小',
        'extra': '描述',
        'sendAsPicture': '已图片形式发送'
      },
      'addContact': {
        'title': '添加联系人',
        'query': '输入联系人账号或者手机号码',
        'phone': '手机号码',
        'notFound': '未找到指定联系人',
        'empty': '请输入',
        'searching': '搜索: "{query}"'
      },
      'createGroup': {
        'title': '创建群组',
        'groupName': '群组名称'
      },
      'quickSearch': {
        'title': '通讯录',
        'placeholder': '输入以开始搜索',
        'toNavigate': ':进行上下选择  ',
        'toSelect': ':选定  ',
        'toClose': ':关闭  ',
        'openDialog': '打开聊天窗口',
        'startDialog': '打开新的聊天传功库',
        'notFound': '没有与<strong>{query}</strong>匹配的联系人.<br/>请重新输入'
      },
      'confirm': {
        'logout': '确定登出吗?',
        'user': {
          'clear': '确定清空与 <strong>{name}</strong>的聊天记录吗?',
          'delete': '确定删除 <strong>{name}</strong>吗?',
          'block': '确定拉黑 <strong>{name}</strong>吗?',
          'removeContact': '确定删除联系人: <strong>{name}</strong> 吗?'
        },
        'group': {
          'clear': '确定清空 <strong>{name}</strong>的聊天记录吗?',
          'delete': '确定删除 <strong>{name}</strong>吗?',
          'leave': '确定离开 <strong>{name}</strong>吗?',
          'kick': '确定将 <strong>{name}</strong>移出群组吗?'
        },
        'nonContactHide': {
          'title': '确定隐藏吗?',
          'body': '用户 {name} 不在你的联系人名单中.'
        },
        delete: '删除群组?',
        kick: '移除这个用户?'
      }
    },

    // Profiles
    'profile': {
      'email': '邮箱',
      'phone': '手机号',
      'nickname': '昵称',
      'about': '简介'
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
          'first': '暂无会话.',
          'second': {
            'start': '你可以 ',
            'or': ' 或者 ',
            'end': '.'
          }
        },
        'newDialog': '创建新的会话',
        'addPeople': 'add people',
        'favourites': '收藏夹',
        'groups': '群组',
        'privates': '聊天记录',
        'history': '过往聊天对象'
      },
      'group': {
        'empty': '创建您的首个群组'
      },
      'private': {
        'empty': '暂时无人在线'
      }
    },

    'main': {
      'empty': 'Try to be better than yesterday!',
      'install': '<h1>The Web version of <b>{appName}</b> only works in desktop browsers at this time</h1>' +
      '<h3>Try our apps for using <b>{appName}</b> on your phone!</h3>' +
      '<p><a href="//actor.im/ios">iPhone</a> | <a href="//actor.im/android">Android</a></p>',
      'deactivated': {
        'header': '',
        'text': 'Oops, we have detected another tab running {appName}! We had to deactivate it to prevent you from dangerous things happening.'
      }
    },

    preferences: {
      title: '设置',
      general: {
        title: '基础',
        send: {
          title: '发送消息',
          sendMessage: '发送消息',
          newLine: '换行'
        }
      },
      notifications: {
        title: '提醒 & 声音',
        effects: {
          title: '音效',
          enable: '开启音效'
        },
        notification: {
          title: '提醒',
          enable: '运行群组提醒',
          onlyMentionEnable: '仅开启与自己相关的提醒',
          onlyMentionHint: '只有当你被人@到时才会发出提醒.'
        },
        privacy: {
          title: '隐私',
          messagePreview: '消息预览',
          messagePreviewHint: '是否在消息提醒中显示消息内容.'
        }
      },
      security: {
        title: '安全',
        sessions: {
          title: '活动连接',
          current: '当前连接',
          authTime: '首次登陆时间',
          terminate: '断开',
          terminateAll: '中断所有其它连接'
        }
      },
      blocked: {
        title: '黑名单',
        notExists: '黑名单为空.',
        notFound: '抱歉,没有找到指定用户.',
        search: '搜索黑名单中的用户',
        unblock: '移除'
      },
      about: {
        title: '关于'
      }
    },

    invite: {
      title: '邀请更多人',
      search: '搜索',
      notFound: '抱歉,没有找到指定用户.',
      inviteByLink: '邀请链接',
      byLink: {
        title: '邀请链接',
        description: '用户可以通过打开链接加入到”<b>{groupName}</b>”',
        copy: '复制',
        revoke: '取消链接'
      }
    },

    call: {
      outgoing: '去电',
      incoming: '来电',
      mute: '静音',
      unmute: '取消静音',
      answer: '接听',
      decline: '拒绝',
      end: '挂断',
      addUser: '添加用户',
      fullScreen: '全屏',
      video: '视频',
      state: {
        calling: '拨号中',
        connecting: '连接中',
        in_progress: '通话时间: {time}',
        ended: '结束'
      }
    },

    tooltip: {
      toolbar: {
        info: '当前会话的相关信息',
        favorite: '收藏'
      },
      recent: {
        groupsList: '群组列表',
        privatesList: '私人会话列表',
        groupsCreate: '创建群组',
        privatesCreate: '添加新联系人'
      },
      quicksearch: '查看所有联系人'
    },

    context: {
      favorite: {
        add: '收藏',
        remove: '取消收藏'
      },
      archive: '移动到过往聊天对象',
      delete: '从会话列表移除'
    },

    search: {
      'placeholder': '搜索',
      'emptyQuery': '输入想要搜索的内容',
      'searching': '搜索: "{query}"',
      'notFound': '没有搜索到与 "{query}"相关的内容',
      hint: '<h4>快速搜索</h4><p>快速搜索用户或者群组</p>',
      inDialog: '聊天历史记录搜索'
    },

    toolbar: {
      search: {
        hint: '输入以开始搜索',
        messages: '聊天历史记录搜索',
        contacts: {
          title: '联系人',
          notFound: '抱歉,没有找到指定联系人=('
        },
        groups: {
          title: '群组',
          notFound: '抱歉,没有找到指定群组 =('
        }
      }
    }
  }
};
