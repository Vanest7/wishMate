import { eventStatusLabels } from '../../shared/domain/event-status';
export const wishesContent = {
  title: 'Lista de deseos',
  description:
    'Comparte lo que te haría ilusión y descubre las ideas de los demás.',
  back: 'Volver al evento',
  navigation: 'Grupo y evento',
  create: 'Añadir deseo',
  createTitle: '¿Qué te haría ilusión?',
  createDescription: 'Este deseo se añadirá a tu lista dentro del evento.',
  edit: 'Editar',
  editTitle: 'Editar mi deseo',
  editDescription: 'Actualiza los detalles para ayudar a los demás a elegir.',
  delete: 'Eliminar',
  deleteTitle: '¿Eliminar este deseo?',
  deleteDescription:
    'Esta acción no se puede deshacer. Confirma que quieres eliminar:',
  cancel: 'Cancelar',
  save: 'Guardar cambios',
  pending: 'Guardando…',
  deleting: 'Eliminando…',
  created: 'Deseo añadido.',
  updated: 'Deseo actualizado.',
  deleted: 'Deseo eliminado.',
  all: 'Todos',
  mine: 'Mis deseos',
  yours: 'Tu deseo',
  owner: 'Deseo de',
  unknownOwner: 'Miembro del grupo',
  loading: 'Cargando deseos…',
  retry: 'Volver a intentar',
  emptyTitle: 'Las buenas ideas empiezan con un deseo',
  emptyDescription: 'Todavía no hay deseos en este evento.',
  emptyMineTitle: 'Todavía no has añadido deseos',
  emptyMineDescription: 'Aquí aparecerán tus ideas de regalo para este evento.',
  name: 'Nombre del deseo',
  nameHint: 'Hasta 150 caracteres.',
  requiredName: 'Escribe un nombre para el deseo.',
  nameTooLong: 'El nombre no puede superar los 150 caracteres.',
  descriptionLabel: 'Descripción',
  descriptionHint:
    'Añade detalles como talla, color o modelo. Hasta 255 caracteres.',
  requiredDescription: 'Escribe una descripción.',
  descriptionTooLong: 'La descripción no puede superar los 255 caracteres.',
  purchaseUrl: 'Enlace de compra',
  urlHint: 'Opcional. Una dirección que empiece por https:// o http://.',
  invalidUrl:
    'Introduce una dirección HTTP o HTTPS válida, de hasta 2048 caracteres.',
  openLink: 'Ver enlace de compra',
  invalidStoredUrl: 'El enlace de compra no está disponible.',
  estimatedPrice: 'Precio orientativo',
  priceHint: 'Opcional. Ej.: 25,50. Sin separador de miles.',
  invalidPrice:
    'Introduce un importe entre 0 y 99999999,99 con un máximo de dos decimales.',
  serverField: 'Revisa este campo.',
  states: eventStatusLabels,
  readOnly: {
    DRAFT: 'Podrás añadir y modificar tus deseos cuando el evento esté activo.',
    ACTIVE: '',
    CLOSED:
      'El evento está cerrado. Puedes consultar los deseos, pero ya no modificarlos.',
    ARCHIVED:
      'El evento está archivado. Puedes consultar los deseos, pero ya no modificarlos.',
  },
  errors: {
    generic: 'No hemos podido completar la operación. Inténtalo de nuevo.',
    network:
      'No se ha podido conectar. Comprueba tu conexión e inténtalo de nuevo.',
    validation:
      'Revisa los datos. Si el estado del evento ha cambiado, cancela y actualiza la vista.',
    forbidden:
      'No se ha autorizado esta operación. Puede que tus permisos hayan cambiado o que sea necesario iniciar sesión de nuevo.',
    missing: 'El grupo, el evento o el deseo ya no está disponible.',
    conflict:
      'La operación no se ha podido completar. Cancela y actualiza la vista antes de volver a intentarlo.',
  },
} as const;
