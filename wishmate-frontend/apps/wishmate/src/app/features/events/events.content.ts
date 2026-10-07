export const eventsContent = {
  title: 'Eventos',
  description: 'Organiza las celebraciones y los regalos que compartiréis.',
  group: 'Grupo',
  create: 'Crear evento',
  createTitle: 'Un nuevo motivo para celebrar',
  createDescription:
    'Elige un nombre, añade los detalles y decide cuándo empezar.',
  edit: 'Editar evento',
  editTitle: 'Editar evento',
  editDescription:
    'Los cambios serán visibles para todos los miembros del grupo.',
  loading: 'Cargando eventos…',
  loadingDetail: 'Cargando el evento…',
  emptyTitle: 'La próxima celebración empieza aquí',
  emptyDescription:
    'Crea el primer evento y compartid vuestras ideas de regalo.',
  open: 'Ver evento',
  back: 'Volver a eventos',
  retry: 'Volver a intentar',
  name: 'Nombre del evento',
  nameHint: 'Hasta 100 caracteres.',
  descriptionLabel: 'Descripción',
  descriptionHint: 'Opcional. Hasta 255 caracteres.',
  startDate: 'Fecha de inicio',
  endDate: 'Fecha de fin',
  optionalDate: 'Opcional',
  initialStatus: 'Estado inicial',
  statusHint:
    'En borrador podéis preparar el evento. Actívalo para añadir deseos y reservar regalos.',
  noDates: 'Sin fechas definidas',
  noDescription: 'Todavía no hay una descripción para este evento.',
  save: 'Guardar cambios',
  cancel: 'Cancelar',
  pending: 'Guardando…',
  required: 'Escribe un nombre para el evento.',
  nameTooLong: 'El nombre no puede superar los 100 caracteres.',
  descriptionTooLong: 'La descripción no puede superar los 255 caracteres.',
  invalidDate: 'Introduce una fecha válida.',
  invalidDateRange: 'La fecha de fin no puede ser anterior a la de inicio.',
  serverField: 'Revisa este campo.',
  created: 'Evento creado.',
  updated: 'Evento actualizado.',
  activate: 'Activar evento',
  close: 'Cerrar evento',
  archive: 'Archivar evento',
  activateTitle: '¿Activar este evento?',
  closeTitle: '¿Cerrar este evento?',
  archiveTitle: '¿Archivar este evento?',
  activateDescription:
    'Los miembros podrán añadir deseos y reservar regalos. No se puede volver al estado de borrador.',
  closeDescription:
    'Ya no se podrán editar el evento ni los deseos, reservar regalos o cancelar reservas. No se puede reabrir.',
  archiveDescription:
    'El evento se conservará para consulta. Archivarlo no lo elimina y no se puede volver al estado anterior.',
  activated: 'Evento activado.',
  closed: 'Evento cerrado.',
  archived: 'Evento archivado.',
  wishes: 'Ver deseos',
  states: {
    DRAFT: 'Borrador',
    ACTIVE: 'Activo',
    CLOSED: 'Cerrado',
    ARCHIVED: 'Archivado',
  },
  stateDescriptions: {
    DRAFT:
      'Prepara los detalles. Activa el evento cuando queráis empezar a añadir deseos.',
    ACTIVE: 'Ya podéis añadir deseos y reservar regalos para otras personas.',
    CLOSED: 'Este evento está cerrado y solo permite consultar sus datos.',
    ARCHIVED: 'Este evento está archivado y se conserva para consulta.',
  },
  errors: {
    generic: 'No hemos podido completar la operación. Inténtalo de nuevo.',
    network:
      'No se ha podido conectar. Comprueba tu conexión e inténtalo de nuevo.',
    validation:
      'Revisa los datos. Si el estado del evento ha cambiado, cancela y actualiza la vista.',
    forbidden:
      'No se ha autorizado esta operación. Puede que tus permisos hayan cambiado o que sea necesario iniciar sesión de nuevo.',
    missing: 'El evento o su grupo ya no está disponible.',
    conflict:
      'El evento ha cambiado o la operación entra en conflicto con su estado. Cancela y actualiza la vista.',
  },
} as const;
