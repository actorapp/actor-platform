export default {
  'locales': 'ru-RU',
  'messages': {
    // Login
    'login': {
      'signIn': 'Вход',
      'wrong': 'Ошиблись?',
      'phone': 'Номер телефона',
      'email': 'Email адрес',
      'authCode': 'Код авторизации',
      'yourName': 'Ваше имя',
      'errors': {
        'numberInvalid': 'Неправильный номер',
        'nameInvalid': ' Неправильное имя',
        'codeInvalid': 'Неправильный код',
        'codeExpired': 'Время действия кода истекло',
        'codeWait': 'Попытайтесь запросить код позже'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      'editProfile': 'Редактировать профиль',
      'addToContacts': 'Добавить контакт',
      'createGroup': 'Создать группу',
      'configureIntegrations': 'Настройка интеграций',
      'helpAndFeedback': 'Помощь и обратная связь',
      'twitter': 'Наш твиттер',
      'preferences': 'Настройки',
      'signOut': 'Выход',
      'homePage': 'Домашняя страницв'
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
      'save': 'Сохранить'
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
        'private': '<p>Это самое начало вашей приватной беседы с <strong>{name}</strong>.</p><p>Все сообщения здесь доступны только вам двоим.</p>',
        'group': {
          'main': '<p>Это самое начало групповой беседы <strong>{name}</strong> созданной {creator}.</p>',
          'you': 'Вами',
          'actions': {
            'start': 'Вы можете ',
            'end': ' в эту группу.',
            'invite': 'пригласить еще людей'
          }
        }
      },
      'loading': 'Загрузка сообщений из истории'
    },

    // Connection state
    'connectionState': {
      'connecting': 'Упс! Похоже соединение с сервером потеряно. Пробуем пересоедениться...',
      'online': 'Вы снова онлайн!',
      'updating': ''
    },

    // Compose
    'compose': {
      'attach': 'Прикрепить',
      'sendFile': 'Документ',
      'sendPhoto': 'Изображение',
      'send': 'Отправить',
      'markdown': {
        'bold': 'жирный',
        'italic': 'курсив',
        'preformatted': 'форматированный'
      },
      'dropzone': 'Для отправки отпустите здесь.'
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
        'avatarRemove': 'Удалить'
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
        'query': 'Никнейм, емайл или телефон',
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
        'notFound': 'По запросу <strong>{query}</strong> ничего не найдено.<br/>Вы уверены в правильности набора?'
      },
      'confirm': {
        'logout': 'Вы действительно хотите уйти?',
        'leave': 'Вы действительно хотите покинуть этот диалог?',
        'clear': 'Вы действительно хотите очистить этот диалог?',
        'delete': 'Вы действительно хотите удалить этот диалог',
        'removeContact': 'Удалить {name} из списка контактов?',
        'kick': 'Вы действительно хотите исклчюить {name}',
        'nonContactHide': {
          'title': 'Вы действительно хотите скрыть этот диалог?',
          'body': '{name} не в списке ваших контактов.'
        }

      }
    },

    // Profiles
    'profile': {
      'email': 'емайл',
      'phone': 'телефон',
      'nickname': 'ник'
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
    'integrationTokenHelp': 'Узнайте как пользоваться',

    // Modals
    'inviteModalTitle': 'Пригласить людей в группу',
    'inviteModalSearch': 'Поиск по именам и никнеймам',
    'inviteModalNotFound': 'Извините, ничего не найдено',
    'inviteByLink': 'Ссылка для приглашения в группу',
    'inviteByLinkModalTitle': 'Пригласить по ссылке',
    'inviteByLinkModalDescription': 'Любой в интернете теперь может присоедениться к ”{groupName}” открыв эту ссылку:',
    'inviteByLinkModalCopyButton': 'Скопировать',
    'inviteByLinkModalRevokeButton': 'Отменить',
    'inviteLinkCopied': 'Ссылка скопирована.',

    'preferencesModalTitle': 'Настройки',
    'preferencesGeneralTab': 'Основные',
    'preferencesNotificationsTab': 'Уведомления и Звуки',
    'preferencesSecurityTab': 'Безопасность',
    'preferencesSendMessageTitle': 'Отправка сообщений',
    'preferencesSendMessage': 'отправить сообщение',
    'preferencesNewLine': 'новая строка',
    'preferencesEffectsTitle': 'Эффекты',
    'preferencesEnableEffects': 'Включить звуковые эффекты',
    'preferencesNotificationsTitle': 'Уведомления',
    'preferencesNotificationsGroup': 'Включить уведомления для групп',
    'preferencesNotificationsOnlyMention': 'Включить уведомления только для упоминаний',
    'preferencesNotificationsOnlyMentionHint': 'Вы можете включить уведомления только для сообщений в которых вы упомянуты.',
    'preferencesPrivacyTitle': 'Конфиденциальность',
    'preferencesMessagePreview': 'Предварительный просмотр сообщений',
    'preferencesMessagePreviewHint': 'Удаляет текст сообщений из уведомлений.',
    'preferencesSessionsTitle': 'Активные сессии',
    'preferencesSessionsCurrentSession': 'Текущий сеанс',
    'preferencesSessionsAuthTime': 'Авторизовано',
    'preferencesSessionsTerminate': 'Завершить',
    'preferencesSessionsTerminateAll': 'Завершить все сеансы',

    // Sidebar
    'sidebar': {
      'recents': {
        'empty': {
          'first': 'В данный момент у вас нет открытых диалогов.',
          'second': {
            'start': 'Вы можете',
            'or': ' или ',
            'end': '.'
          }
        },
        'newDialog': 'создать диалог',
        'addPeople': 'добавить контакт'
      }
    }
  }
};
