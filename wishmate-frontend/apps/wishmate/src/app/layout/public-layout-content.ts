export const publicLayoutContent = {
  navigation: {
    home: 'Ir al inicio',
    login: 'Entrar',
    register: 'Crear cuenta',
    skipToContent: 'Saltar al contenido',
  },
  welcome: {
    eyebrow: 'Pequeños detalles, grandes sonrisas',
    title: 'Acertar con un regalo',
    highlight: 'empieza por compartir.',
    description:
      'Reúne a tu gente, comparte tus deseos y prepara sorpresas para los momentos que importan.',
  },
  features: [
    {
      id: 'groups',
      title: 'Tu gente, en un mismo lugar',
      description: 'Crea grupos con familiares y amigos.',
    },
    {
      id: 'wishes',
      title: 'Deseos que se hacen realidad',
      description: 'Comparte tus ideas para cada celebración.',
    },
    {
      id: 'surprises',
      title: 'La sorpresa queda entre nosotros',
      description: 'Organizad los regalos sin desvelarlos a quien los recibe.',
    },
  ],
  access: {
    label: 'Acceso a WishMate',
    note: 'Para celebrar juntos, incluso antes de abrir el regalo.',
  },
  footer: {
    brand: 'WishMate',
    tagline: 'Regalos con intención. Sorpresas con corazón.',
  },
} as const;
