export default {
  'locale': 'es',
  'messages': {
    // Login
    'login': {
      'signIn': 'Registrarse',
      'wrong': '¿Equivocado?',
      'phone': 'Número de móvil',
      'email': 'Dirección de email',
      'authCode': 'Código verificación',
      'yourName': 'Su nombre',
      'errors': {
        'numberInvalid': 'Número no válido',
        'nameInvalid': 'Nombre no válido',
        'codeInvalid': 'Código no válido',
        'codeExpired': 'Código experido',
        'codeWait': 'Vulve a solicitar pasado un minuto'
      },
      'welcome': {
        'header': '<strong>{appName}</strong> Mensajería, gratis, y veloz.',
        'text': '<p>Siente la libertad de comunicar desde cualquier dispositivo, móvil, pc, mac o tablets desde cualquier lugar del mundo solo con conexión wifi, 3G o 4G.</p>' +
                '<p>Olvida para siempre el viejo correo electrónico y mensajes para tu negocio. {appName} plataforma centra todo en una sola aplicación.</p>'
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
      'close': 'Cerca',
      'save': 'Guardar',
      'call': 'Llamada',
      'archive': 'Todos'
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
        'private': '<p>Este es el inicio de tu conversación privada con <strong>{name}</strong>.</p><p>Todos los mensajes son encriptados entre los dos usuarios.</p>',
        'group': {
          'main': '<p>Este es el principio de <strong>{name}</strong> grupo de conversación que creado por {creator}.</p>',
          'you': 'usted',
          'actions': {
            'start': 'Puede invitar ',
            'end': ' a esta conversación.',
            'invite': 'a más usuarios'
          }
        }
      },
      'loading': 'Cargando historial de mensajes'
    },

    // Connection state
    'connectionState': {
      'connecting': '¡Houston, tenemos un problema! Conexión con {appName} servidor se pierde. Intentando reconectar ahora...',
      'online': 'Estás de vuelta en línea!',
      'updating': ''
    },

    // Compose
    'compose': {
      'attach': 'Adjuntar',
      'sendFile': 'Enviar archivo',
      'sendPhoto': 'Enviar foto',
      'send': 'Enviar',
      'markdown': {
        'bold': 'negrita',
        'italic': 'cursiva',
        'preformatted': 'preformateado'
      },
      'dropzone': 'Colocar aquí para enviar.',
      'start': 'Empezar',
      'unblock': 'Desbloquear usuario'
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
        'notFound': 'Lo sentimos, no hay usuarios encontrados.',
        'loading': 'Cargando'
      },
      'groups': {
        'title': 'Grupos',
        'search': 'Buscar',
        'loading': 'Cargando',
        'notFound': 'No se han encontrado coincidencias <strong>{query}</strong>.'
      },
      'attachments': {
        'title': 'Envier archivo',
        'name': 'Nombre del archivo',
        'type': 'Tipo',
        'size': 'Tamaño',
        'extra': 'Extra',
        'sendAsPicture': 'Enviar esta imagen'
      },
      'addContact': {
        'title': 'Añadir contacto',
        'query': 'Correo electrónico, teléfono o sobrenombre',
        'phone': 'Número de móvil',
        'notFound': 'El usuario con dichos datos no se encuentra',
        'empty': 'Comience a escribir para buscar personas',
        'searching': 'Buscar "{query}"'
      },
      'createGroup': {
        'title': 'Crear grupo',
        'groupName': 'Nombre de grupo'
      },
      'quickSearch': {
        'title': 'Buscar en todas partes',
        'placeholder': 'Empieza a escribir',
        'toNavigate': 'para navegar',
        'toSelect': 'para seleccionar',
        'toClose': 'para cerrar',
        'openDialog': 'Abrir conversación',
        'startDialog': 'Comenzar una nueva conversación',
        'notFound': 'No se han encontrado coincidencias <strong>{query}</strong>.<br/>Tiene escrito correctamente?'
      },
      'confirm': {
        'logout': '¿De verdad quieres salir?',
        'user': {
          'clear': '¿Realmente desea borrar esta conversación?',
          'delete': '¿De verdad quiere eliminar esta conversación?',
          'block': '¿Usted realmente desea bloquear {name}?',
          'removeContact': '¿De verdad quiere eliminar {name} de tus contactos?'
        },
        'group': {
          'clear': '¿Realmente desea borrar esta conversación?',
          'delete': '¿De verdad quiere eliminar esta conversación?',
          'leave': '¿De verdad quieres salir de esta conversación?',
          'kick': '¿Seguro que deseas expulsar a {name}'
        },
        'nonContactHide': {
          'title': '¿Está seguro de que desea ocultar esta conversación?',
          'body': 'Usuario {name} no está en su lista de contactos.'
        }
      }
    },

    // Profiles
    'profile': {
      'email': 'correo electrónico',
      'phone': 'móvil',
      'nickname': 'nick usuario',
      'about': 'estado'
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
    'blockUser': 'Bloquear usuario',
    'unblockUser': 'Desbloquear usuario',
    'leaveGroup': 'Abandonar grupo',
    'sharedMedia': 'Medios compartidos',
    'notifications': 'Notificationes',
    'integrationTokenCopied': 'Enlace Integración copiado.',
    'members': '{numMembers, plural,' +
      '=0 {Sin Miembros}' +
      '=1 {# Miembro}' +
      'other {# Miembros}' +
    '}',
    'kick': 'Expulsar',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does, this integration token allow the most flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Sidebar
    'sidebar': {
      'recents': {
        'groups': 'Grupos',
        'privates': 'Mensajes directos'
      },
      'group': {
        'empty': 'Crea tu primer grupo de conversación'
      },
      'private': {
        'empty': 'No hay nadie en su red'
      }
    },

    'main': {
      'empty': 'Seleccione diálogo o iniciar uno nuevo.',
      'install': '<h1>Versión Web de <b>{appName}</b> Versión solo para navegadores</h1>' +
                 '<h3>Link de aplicaciones móviles <b>{appName}</b>.</h3>',
      'deactivated': {
        'header': 'Pestaña desactivada',
        'text': 'Vaya, ha abierto otra sesión con {appName}, así que tuvimos que desactivar ésta para evitar posibles errores.'
      }
    },

    preferences: {
      title: 'Preferencias',
      general: {
        title: 'General',
        send: {
          title: 'Enviar mensaje',
          sendMessage: 'enviar mensaje',
          newLine: 'nueva línea'
        }
      },
      notifications: {
        title: 'Notificaciones & Sonidos',
        effects: {
          title: 'Efectos',
          enable: 'Habilitar efectos de sonido'
        },
        notification: {
          title: 'Notificaciones',
          enable: 'Habilitar notificaciones de grupo',
          onlyMentionEnable: 'Habilitar notificaciones solo por mención',
          onlyMentionHint: 'Puedes activar las notificaciones sólo para mensajes que contiene lo que mencionas.'
        },
        privacy: {
          title: 'Privacidad',
          messagePreview: 'Mensaje de previsualización',
          messagePreviewHint: 'Retire el texto del mensaje de notificaciones.'
        }
      },
      security: {
        title: 'Seguridad',
        sessions: {
          title: 'Sesiones activas',
          current: 'Sesión actual',
          authTime: 'Fecha de acceso',
          terminate: 'Eliminar',
          terminateAll: 'Terminar todas las sesiones'
        }
      },
      blocked: {
        title: 'Los Usuarios Bloqueados',
        notExists: 'Aún no bloquear a nadie.',
        notFound: 'Lo sentimos, no se encontraron usuarios.',
        search: 'Búsqueda de contactos o nombres de usuario',
        unblock: 'Desbloquear'
      },
      about: {
        title: 'About'
      }
    },

    invite: {
      title: 'Añadir más personas',
      search: 'Búsqueda de contactos o nombres de usuario',
      notFound: 'Lo sentimos, no hay usuarios localizados.',
      inviteByLink: 'Invitar al grupo por enlace',
      byLink: {
        title: 'Invitar por enlace',
        description: 'Cualquier persona en la web será capaz de unirse a ”{groupName}” abriendo este enlace:',
        copy: 'Copiar enlace',
        copied: 'Enlace Invitación copiado.',
        revoke: 'Revocar enlace'
      }
    },

    call: {
      outgoing: 'Llamada saliente',
      incoming: 'Llamada entrante',
      mute: 'Silenciar',
      unmute: 'Activado',
      answer: 'Responder',
      decline: 'Disminución',
      end: 'Llamada finalizada',
      addUser: 'Añadir usury',
      fullScreen: 'Pantalla completa',
      video: 'Vídeo'
    },

    toolbar: {
      callState: {
        calling: 'Llamando',
        connecting: 'Conectando',
        in_progress: 'Llamada activa: {time}',
        ended: 'Llamada finalizada'
      }
    },

    context: {
      favorite: {
        add: 'Favorito',
        remove: 'Eliminar de favoritos'
      },
      archive: 'Enviar a archivar',
      delete: 'Eliminar'
    }
  }
};
