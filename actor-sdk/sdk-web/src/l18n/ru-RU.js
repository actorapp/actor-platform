export default {
  'locale': 'ru',
  'messages': {
    // Login
    'login': {
      'signIn': 'Вход',
      'wrong': 'Ошиблись?',
      'phone': 'Номер телефона',
      'email': 'Email адрес',
      'phone_or_email': 'Номер телефона или Email-адрес',
      'authCode': 'Код авторизации',
      'yourName': 'Ваше имя',
      'errors': {
        'numberInvalid': 'Некорректный номер',
        'nameInvalid': ' Некорректное имя',
        'codeInvalid': 'Неверный код',
        'codeExpired': 'Время действия кода истекло',
        'codeWait': 'Попытайтесь запросить код позже'
      },
      'welcome': {
        'header': 'Добро пожаловать в <strong>{appName}</strong>'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      editProfile: 'Редактировать профиль',
      addToContacts: 'Добавить контакт',
      createGroup: 'Создать группу',
      blockedUsers: 'Заблокированные пользователи',
      helpAndFeedback: 'Помощь и обратная связь',
      twitter: 'Наш твиттер',
      preferences: 'Настройки',
      signOut: 'Выход',
      homePage: 'Домашняя страница',
      facebook: 'Ищите нас в Facebook'
    },

    // Buttons
    'button': {
      'ok': 'Ok',
      'cancel': 'Отмена',
      'done': 'Готово',
      'requestCode': 'Запросить код',
      'checkCode': 'Проверить код',
      'signUp': 'Регистрация',
      'add': 'Добавить',
      'send': 'Отправить',
      'sendAll': 'Отправить все',
      'createGroup': 'Создать группу',
      'addMembers': 'Добавить пользователей',
      'quickSearch': 'Быстрый поиск',
      'close': 'Закрыть',
      'save': 'Сохранить',
      'call': 'Позвонить',
      'archive': 'Архив',
      'invite': '+ Пригласить людей'
    },

    // Messages
    'message': {
      'pin': 'Прикрепить',
      'reply': 'Ответить',
      'forward': 'Переслать',
      'download': 'Скачать',
      'delete': 'Удалить',
      'quote': 'Цитировать',
      'uploading': 'Загрузка...',
      'welcome': {
        'private': '<p>Это начало вашей приватной беседы с <strong>{name}</strong>.</p>' +
                   '<p>Все сообщения доступны только вам двоим.</p>',
        'group': {
          'main': '<p>Это начало групповой беседы <strong>{name}</strong> созданной {creator}.</p>',
          'you': 'Вами',
          'actions': {
            'start': 'Вы можете ',
            'end': ' в эту группу.',
            'invite': 'пригласить еще людей'
          }
        }
      },
      'loading': 'Загрузка сообщений из истории',
      'unread': 'Новые сообщения'
    },

    // Connection state
    'connectionState': {
      'connecting': 'Упс! Похоже соединение с сервером потеряно. Пробуем переподключиться…',
      'online': 'Вы снова онлайн!',
      'updating': 'Обновление соединения'
    },

    // Compose
    'compose': {
      'attach': 'Прикрепить',
      'sendFile': 'Документ',
      'sendPhoto': 'Изображение',
      'send': 'Отправить',
      'edit': 'Сохранить',
      'cancel': 'Отменить',
      'editTitle': 'Редактирование сообщения:',
      'markdown': {
        'bold': 'жирный',
        'italic': 'курсив',
        'preformatted': 'форматированный'
      },
      'notMember': 'Вы не участник данной группы',
      'unblock': 'Разблокировать пользователя',
      'start': 'Начать',
      'dropzone': 'Перетащите файлы сюда для отправки.'
    },

    // Modals
    'modal': {
      'profile': {
        'title': 'Профиль',
        'name': 'Ваше имя',
        'nick': 'Никнейм',
        'phone': 'Номер телефона',
        'email': 'Email',
        'about': 'О себе',
        'avatarChange': 'Изменить аватар',
        'avatarRemove': 'Удалить',
        errors: {
          nick: {
            length: 'Никнейм должен содержать от 5 до 32 символов',
            chars: 'Пожалуйста используйте латинские буквы, цифры или подчеркивание'
          }
        }
      },
      'group': {
        'title': 'Редактировать группу',
        'name': 'Название группы',
        'about': 'Описание',
        'avatarChange': 'Изменить аватар',
        'avatarRemove': 'Удалить'
      },
      'crop': {
        'title': 'Кадрирование'
      },
      'contacts': {
        'title': 'Люди',
        'search': 'Поиск контактов',
        'loading': 'Загружается',
        'notFound': 'Извините, ничего не найдено.'
      },
      'groups': {
        'title': 'Группы',
        'search': 'Поиск по группам',
        'loading': 'Загрузка',
        'notFound': 'По запросу <strong>{query}</strong> ничего не найдено'
      },
      'attachments': {
        'title': 'Отправить файл',
        'name': 'Имя файла',
        'type': 'Тип',
        'size': 'Размер',
        'extra': 'Дополнительно',
        'sendAsPicture': 'Отправить как изображение'
      },
      'addContact': {
        'title': 'Добавить контакт',
        'query': 'Никнейм, email или телефон',
        'phone': 'Номер телефона',
        'notFound': 'Пользователь с такими данными не найден.',
        'empty': 'Начните печатать для поиска людей',
        'searching': 'Поиск пользователя "{query}"'
      },
      'createGroup': {
        'title': 'Создать группу',
        'groupName': 'Название группы'
      },
      'quickSearch': {
        'title': 'Быстрый поиск',
        'placeholder': 'Начните печатать',
        'toNavigate': 'для навигации',
        'toSelect': 'для выбора',
        'toClose': 'для отмены',
        'openDialog': 'Открыть диалог',
        'startDialog': 'Начать новый диалог',
        'notFound': 'По запросу <strong>{query}</strong> ничего не найдено.<br/>Вы уверены, что правильно составили запрос?'
      },
      'confirm': {
        'logout': 'Вы действительно хотите уйти?',
        'user': {
          'clear': 'Вы действительно хотите очистить диалог с {name}?',
          'delete': 'Вы действительно хотите удалить диалог с {name}?',
          'block': 'Вы действительно хотите заблокировать {name}?',
          'removeContact': 'Are you sure you want to remove {name} from your contacts?'
        },
        'group': {
          'clear': 'Вы действительно хотите очистить диалог {name}?',
          'delete': 'Вы действительно хотите удалить диалог {name}?',
          'leave': 'Вы действительно хотите покинуть диалог {name}?',
          'kick': 'Вы действительно хотите исключить {name}?'
        },
        'nonContactHide': {
          'title': 'Вы действительно хотите скрыть этот диалог?',
          'body': '{name} не в списке ваших контактов.'
        },
        delete: 'Удалить эту группу?',
        kick: 'Исключить этого пользователя?'
      }
    },

    // Profiles
    'profile': {
      'email': 'email',
      'phone': 'телефон',
      'nickname': 'ник',
      'about': 'о себе'
    },
    'createdBy': 'создал',
    'addPeople': 'Пригласить',
    'more': 'Еще',
    'actions': 'Действия',
    'addToContacts': 'Добавить в контакты',
    'removeFromContacts': 'Удалить из контактов',
    'setGroupPhoto': 'Установить фото группы',
    'addIntegration': 'Добавить интеграцию',
    'editGroup': 'Редактировать группу',
    'clearGroup': 'Очистить группу',
    'deleteGroup': 'Удалить группу',
    'clearConversation': 'Очистить диалог',
    'deleteConversation': 'Удалить диалог',
    'blockUser': 'Заблокировать',
    'unblockUser': 'Разблокировать',
    'leaveGroup': 'Покинуть группу',
    'sharedMedia': 'Вложения',
    'notifications': 'Уведомления',
    'integrationTokenCopied': 'Ссылка скопирована.',
    'members': '{numMembers, plural,' +
      '=0 {Нет участников}' +
      '=1 {# участник} =2 {# участника} =3 {# участника} =4 {# участника}' +
      'other {# участников}' +
    '}',
    'kick': 'Исключить',
    'integrationToken': 'Интеграция',
    'integrationTokenHint': 'Вы можете использовать этот токен для настройки интеграции с вашими собственными системами.',
    'integrationTokenHelp': 'Узнайте, как пользоваться токенами',

    // Sidebar
    'sidebar': {
      'recents': {
        'groups': 'Группы',
        'privates': 'Диалоги',
        'empty': {
          'first': 'У вас нет открытых диалогов.',
          'second': {
            'start': 'Вы можете ',
            'or': ' или ',
            'end': '.'
          }
        },
        'newDialog': 'создать диалог',
        'favourites': 'Избранное',
        'addPeople': 'добавить контакт',
        history: 'История'
      },
      'group': {
        'empty': 'Создайте ваш первый групповой диалог'
      },
      'private': {
        'empty': 'В вашей сети пока никого нет'
      }
    },

    'main': {
      'empty': 'Старайся быть лучше, чем был вчера!',
      'deactivated': {
        'header': 'Вкладка деактивирована',
        'text': 'Упс, похоже что вы открыли еще одну вкладку с {appName}. Мы вынуждены деактивировать эту вкладку, чтобы избежать ошибок.'
      }
    },

    preferences: {
      title: 'Настройки',
      general: {
        title: 'Основные',
        send: {
          title: 'Отправка сообщений',
          sendMessage: 'отправить сообщение',
          newLine: 'новая строка'
        }
      },
      interface: {
        title: 'Интерфейс',
        animation: {
          title: 'Сразу запускать анимацию'
        }
      },
      notifications: {
        title: 'Уведомления и Звуки',
        effects: {
          title: 'Эффекты',
          enable: 'Включить звуковые эффекты'
        },
        notification: {
          title: 'Уведомления',
          enable: 'Включить уведомления для групп',
          onlyMentionEnable: 'Включить уведомления только для упоминаний',
          onlyMentionHint: 'Вы можете включить уведомления только для сообщений, в которых вы упомянуты.'
        },
        privacy: {
          title: 'Конфиденциальность',
          messagePreview: 'Предварительный просмотр сообщений',
          messagePreviewHint: 'Удаляет текст сообщений из уведомлений.'
        }
      },
      security: {
        title: 'Безопасность',
        sessions: {
          title: 'Активные сессии',
          current: 'Текущий сеанс',
          authTime: 'Авторизовано',
          terminate: 'Завершить',
          terminateAll: 'Завершить все сеансы'
        }
      },
      blocked: {
        title: 'Заблокированные пользователи',
        notExists: 'Вы никого не заблокировали.',
        notFound: 'Извините, ничего не найдено',
        search: 'Поиск по именам и никнеймам',
        unblock: 'Помиловать'
      }
    },

    invite: {
      title: 'Пригласить людей в группу',
      search: 'Поиск по именам и никнеймам',
      notFound: 'Извините, ничего не найдено.',
      inviteByLink: 'Ссылка для приглашения в группу',
      byLink: {
        title: 'Пригласить по ссылке',
        description: 'Любой в интернете теперь может присоедениться к ”<b>{groupName}</b>”, открыв эту ссылку:',
        copy: 'Скопировать',
        revoke: 'Отменить'
      }
    },

    call: {
      outgoing: 'Исходящий вызов',
      incoming: 'Входящий вызов',
      mute: 'Отключить звук',
      unmute: 'Включить звук',
      answer: 'Ответить',
      decline: 'Отменить',
      end: 'Завершить вызов',
      addUser: 'Добавить пользователя',
      fullScreen: 'Полный экран',
      video: 'Видео',
      state: {
        calling: 'звоню',
        connecting: 'соединение',
        in_progress: 'Звонок: {time}',
        ended: 'завершен'
      }
    },

    tooltip: {
      toolbar: {
        info: 'Информация о текущей беседе',
        favorite: 'Добавить/удалить в избранное'
      },
      recent: {
        groupsList: 'Список групповых диалогов',
        privatesList: 'Список приватных диалогов',
        groupsCreate: 'Создать группу',
        privatesCreate: 'Добавить новый контакт'
      },
      quicksearch: 'Быстрый поиск по приложению.'
    },

    context: {
      favorite: {
        add: 'Добавить в избранное',
        remove: 'Убрать из избранного'
      },
      archive: 'Отправить в архив',
      delete: 'Удалить'
    },

    search: {
      placeholder: 'Поиск',
      emptyQuery: 'Начните печатать чтобы начать поиск',
      searching: 'Поиск "{query}"',
      notFound: 'По запросу "{query}" ничего не найдено<br/>Попробуйте поискать что-нибуь другое',
      hint: '<h4>Быстрый поиск</h4><p>Вы можетеискать контакты, группы или сообщения в текущем диалоге отсюда.</p>',
      inDialog: 'Искать сообщения в текущем диалоге'
    }
  }
};
