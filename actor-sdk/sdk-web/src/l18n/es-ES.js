export default {
  'locales': 'es-ES',
  'messages': {
    // Login
    'login': {
      'signIn': 'Registrarse',
      'wrong': '¿Equivocado?',
      'phone': 'Número de móvil',
      'email': 'Email address',
      'authCode': 'Código verificación',
      'yourName': 'Su nombre',
      'errors': {
        'numberInvalid': 'Número no válido',
        'nameInvalid': 'Nombre no válido',
        'codeInvalid': 'Código no válido',
        'codeExpired': 'Phone code is expired',
        'codeWait': 'Try to request code later'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      'editProfile': 'Editar perfil',
      'addToContacts': 'Añadir contacto',
      'createGroup': 'Crear grupo',
      'helpAndFeedback': 'Ayuda',
      'twitter': 'Nuestro Twitter',
      'preferences': 'Preferencias',
      'signOut': 'Salir',
      'homePage': 'Portada del sitio'
    },

    // Buttons
    'button': {
      'ok': 'Ok',
      'cancel': 'Cancelar',
      'done': 'Hecho',
      'requestCode': 'Solicitar código',
      'checkCode': 'Comprobar código',
      'signUp': 'Registrarse',
      'add': 'Añadir',
      'send': 'enviar',
      'sendAll': 'Enviará toda',
      'createGroup': 'Crear grupo',
      'addMembers': 'Añadir miembros',
      'quickSearch': 'Búsqueda rápida',
      'close': 'Cerca'
    },

    // Messages
    'message': {
      'pin': 'Alfiler',
      'reply': 'Responda',
      'forward': 'Reenviar',
      'download': 'Descargar',
      'delete': 'Eliminar',
      'quote': 'Citar',
      'uploading': 'Carga...',
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
      'attach': 'Acoplamiento',
      'sendFile': 'Enviar archivo',
      'sendPhoto': 'Enviar foto',
      'send': 'Enviar',
      'markdown': {
        'bold': 'negrita',
        'italic': 'cursiva',
        'preformatted': 'preformateado'
      },
      'dropzone': 'Colocar aquí para enviar.'
    },

    // Modals
    'modal': {
      'profile': {
        'title': 'Perfil',
        'name': 'Nombre completo',
        'nick': 'Nick usuario',
        'phone': 'Número de móvil',
        'email': 'Email',
        'about': 'Estado',
        'avatarChange': 'Cambiar avatar',
        'avatarRemove': 'Eliminar'
      },
      'group': {
        'title': 'Editar grupo',
        'name': 'Nombre del grupo',
        'about': 'Grupo acerca de',
        'avatarChange': 'Cambiar avatar',
        'avatarRemove': 'Eliminar'
      },
      'crop': {
        'title': 'Recortar la imagen'
      },
      'contacts': {
        'title': 'Personas',
        'search': 'Buscar contactos',
        'notFound': 'Lo sentimos, no hay usuarios encontrados.'
      },
      'groups': {
        'title': 'Grupos',
        'search': 'Buscar',
        'loading': 'Cargando',
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
        'title': 'Añadir contacto',
        'query': 'Correo electrónico, teléfono o sobrenombre',
        'phone': 'Número de móvil',
        'notFound': 'User with such data is not found',
        'empty': 'Start typing to search people',
        'searching': 'Search for "{query}"'
      },
      'createGroup': {
        'title': 'Crear grupo',
        'groupName': 'Nombre de grupo'
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
      'email': 'correo electrónico',
      'phone': 'móvil',
      'nickname': 'nick usuario'
    },
    'createdBy': 'сreado por',
    'addPeople': 'Añadir personas',
    'more': 'Más',
    'actions': 'Acciones',
    'addToContacts': 'Añadir contactos',
    'removeFromContacts': 'Eliminar de los contactos',
    'setGroupPhoto': 'Establecer foto de Grupo',
    'addIntegration': 'Añadir Integración de Servicios',
    'editGroup': 'Editar grupo',
    'clearGroup': 'Limpiar grupo',
    'deleteGroup': 'Eliminar grupo',
    'clearConversation': 'Limpiar Conversación',
    'deleteConversation': 'Eliminar Conversación',
    'leaveGroup': 'Abandonar grupo',
    'sharedMedia': 'Medios compartidos',
    'notifications': 'Notificationes',
    'integrationTokenCopied': 'Enlace Integración copiado.',
    'members': '{numMembers, plural,' +
      '=0 {Sin Miembros}' +
      '=1 {# Miembro}' +
      'other {# Miembros}' +
    '}',
    'kick': 'Kick',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does, this integration token allow the most flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Modals
    'inviteModalTitle': 'Añadir más personas',
    'inviteModalSearch': 'Búsqueda de contactos o nombres de usuario',
    'inviteModalNotFound': 'Lo sentimos, no hay usuarios localizados.',
    'inviteByLink': 'Invitar al grupo por enlace',
    'inviteByLinkModalTitle': 'Invitar por enlace',
    'inviteByLinkModalDescription': 'Cualquier persona en la web será capaz de unirse a ”{groupName}” abriendo este enlace:',
    'inviteByLinkModalCopyButton': 'Copiar enlace',
    'inviteByLinkModalRevokeButton': 'Revocar enlace',
    'inviteLinkCopied': 'Enlace Invitación copiado.',

    'preferencesModalTitle': 'Preferencias',
    'preferencesGeneralTab': 'General',
    'preferencesNotificationsTab': 'Notificaciones & Sonidos',
    'preferencesSecurityTab': 'Seguridad',
    'preferencesSendMessageTitle': 'Enviar mensaje',
    'preferencesSendMessage': 'enviar mensaje',
    'preferencesNewLine': 'nueva línea',
    'preferencesEffectsTitle': 'Efectos',
    'preferencesEnableEffects': 'Habilitar efectos de sonido',
    'preferencesNotificationsTitle': 'Notificaciones',
    'preferencesNotificationsGroup': 'Habilitar notificaciones de grupo',
    'preferencesNotificationsOnlyMention': 'Enable mention only notifications',
    'preferencesNotificationsOnlyMentionHint': 'You can enable notifications only for messages that contains you mention.',
    'preferencesPrivacyTitle': 'Privacy',
    'preferencesMessagePreview': 'Message preview',
    'preferencesMessagePreviewHint': 'Remove message text from notifications.',
    'preferencesSessionsTitle': 'Sesiones activas',
    'preferencesSessionsCurrentSession': 'Current session',
    'preferencesSessionsAuthTime': 'Fecha de acceso',
    'preferencesSessionsTerminate': 'Eliminar',
    'preferencesSessionsTerminateAll': 'Terminar todas las sesiones'
  }
};
