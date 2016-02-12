export default {
  'locale': 'pt',
  'messages': {
    // Login
    'login': {
      'signIn': 'Entrar',
      'wrong': 'Errado?',
      'phone': 'Numero do celular',
      'email': 'Email address',
      'authCode': 'Código de autenticação',
      'yourName': 'Seu nome',
      'errors': {
        'numberInvalid': 'Numero inválido',
        'nameInvalid': 'Nome inválido',
        'codeInvalid': 'Código inválido',
        'codeExpired': 'Este código expirou',
        'codeWait': 'Try to request code later'
      }
    },

    // Menus
    'menu': {
      // Sidebar menu
      'editProfile': 'Editar perfil',
      'addToContacts': 'Adicionar contato',
      'createGroup': 'Criar grupo',
      'configureIntegrations': 'Configure Integrações',
      'helpAndFeedback': 'Ajuda & Feedback',
      'twitter': 'Nosso twitter',
      'preferences': 'Preferências',
      'signOut': 'Sair'
    },

    // Buttons
    'button': {
      'ok': 'Ok',
      'cancel': 'Cancelar',
      'done': 'Feito',
      'requestCode': 'Requisitar código',
      'checkCode': 'Checar código',
      'signUp': 'Cadastrar',
      'add': 'Adicionar',
      'send': 'Send',
      'sendAll': 'Send all',
      'createGroup': 'Criar grupo',
      'addMembers': 'Adicionar usuários',
      'quickSearch': 'Quick search',
      'close': 'Close'
    },

    // Messages
    'message': {
      'pin': 'Pin',
      'reply': 'Reply',
      'forward': 'Forward',
      'download': 'Download',
      'delete': 'Deletar',
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
      'sendFile': 'Enviar arquivo',
      'sendPhoto': 'Enviar foto',
      'send': 'Enviar',
      'markdown': {
        'bold': 'bold',
        'italic': 'italico',
        'preformatted': 'pré formatado'
      },
      'dropzone': 'Drop here to send.'
    },

    // Modals
    'modal': {
      'profile': {
        'title': 'Perfil',
        'name': 'Nome completo',
        'nick': 'Apelido',
        'phone': 'Numero de celular',
        'email': 'Email',
        'about': 'Sobre',
        'avatarChange': 'Mudar foto',
        'avatarRemove': 'Remover'
      },
      'group': {
        'title': 'Editar grupo',
        'name': 'Nome do grupo',
        'about': 'Sobre o grupo',
        'avatarChange': 'Mudar imagem',
        'avatarRemove': 'Remover'
      },
      'crop': {
        'title': 'Cortat imagem'
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
        'title': 'Adicionar contato',
        'query': 'Email, nickname or phone',
        'phone': 'Numero do celular',
        'notFound': 'User with such data is not found',
        'empty': 'Start typing to search people',
        'searching': 'Search for "{query}"'
      },
      'createGroup': {
        'title': 'Criar grupo',
        'groupName': 'Grupo'
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
    'createdBy': 'сriado por',
    'addPeople': 'Adicionar pessoas',
    'more': 'Mais',
    'actions': 'Ações',
    'addToContacts': 'Adicionar a contatos',
    'removeFromContacts': 'Remover dos contatos',
    'setGroupPhoto': 'Escolhe uma imagem do grupo',
    'addIntegration': 'Adicionar um serviço integrado',
    'editGroup': 'Editar Grupo',
    'clearGroup': 'Limpar Grupo',
    'deleteGroup': 'Deletar Grupo',
    'clearConversation': 'Limpar Conversa',
    'deleteConversation': 'Deletar Conversa',
    'leaveGroup': 'Sair do Grupo',
    'sharedMedia': 'Media Compartilhada',
    'notifications': 'Notificações',
    'integrationTokenCopied': 'Link de integração.',
    'members': '{numMembers, plural,' +
    '=0 {sem Membros}' +
    '=1 {# Membro}' +
    'other {# Membros}' +
    '}',
    'kick': 'Kick',
    'integrationToken': 'Integration Token',
    'integrationTokenHint': 'If you have programming chops, or know someone who does, this integration token allow the most flexibility and communication with your own systems.',
    'integrationTokenHelp': 'Learn how to integrate',

    // Modals
    'inviteModalTitle': 'Adicionar mais pessoas',
    'inviteModalSearch': 'Procure por contatos ou usuarios',
    'inviteModalNotFound': 'Desculpe, sem usuários encontrados.',
    'inviteByLink': 'Convidar para o grupo via link',
    'inviteByLinkModalTitle': 'Convidar por link',
    'inviteByLinkModalDescription': 'Qualquer pessoa na web será capaz de juntar-se a ”{groupName}” Abrindo este link:',
    'inviteByLinkModalCopyButton': 'Copiar link',
    'inviteByLinkModalRevokeButton': 'Revogar link',
    'inviteLinkCopied': 'Link de convite copiado.',

    'preferencesModalTitle': 'Preferências',
    'preferencesGeneralTab': 'Geral',
    'preferencesNotificationsTab': 'Notificaões & Sons',
    'preferencesSecurityTab': 'Segurança',
    'preferencesSendMessageTitle': 'Enviar Mensagem',
    'preferencesSendMessage': 'enviar mensagem',
    'preferencesNewLine': 'nova linha',
    'preferencesEffectsTitle': 'Efeitos',
    'preferencesEnableEffects': 'Habilitar sons e efeitos',
    'preferencesNotificationsTitle': 'Notificações',
    'preferencesNotificationsGroup': 'Habilitar notificações do grupo',
    'preferencesNotificationsOnlyMention': 'Ativar apenas notificações com menções',
    'preferencesNotificationsOnlyMentionHint': 'Você pode ativar as notificações apenas para mensagens que contém menções a você.',
    'preferencesPrivacyTitle': 'Privacidade',
    'preferencesMessagePreview': 'Preview de mensagens',
    'preferencesMessagePreviewHint': 'Remover textos das notificações.',
    'preferencesSessionsTitle': 'Sessões ativas',
    'preferencesSessionsCurrentSession': 'Sessão atual',
    'preferencesSessionsAuthTime': 'Tempo logado',
    'preferencesSessionsTerminate': 'Matar sessão',
    'preferencesSessionsTerminateAll': 'Terminar todas seções'
  }
};
