export const authContent = {
  login: {
    title: 'Iniciar sesión',
    description: 'Tus deseos y los de tu gente, en un mismo lugar.',
    submit: 'Entrar en WishMate',
    pending: 'Iniciando sesión…',
    switchPrompt: '¿Todavía no tienes cuenta?',
    switchLink: 'Crea tu cuenta',
  },
  register: {
    title: 'Crear cuenta',
    description: 'Empieza a compartir deseos y a preparar sorpresas.',
    submit: 'Crear mi cuenta',
    pending: 'Creando tu cuenta…',
    switchPrompt: '¿Ya tienes cuenta?',
    switchLink: 'Inicia sesión',
    successTitle: 'Tu cuenta ya está lista',
    successDescription:
      'Ahora puedes iniciar sesión con tu correo y contraseña.',
  },
  fields: {
    userName: 'Nombre completo',
    handle: 'Nombre de usuario',
    email: 'Correo electrónico',
    password: 'Contraseña',
    confirmPassword: 'Repite la contraseña',
  },
  showPassword: 'Mostrar',
  hidePassword: 'Ocultar',
  showPasswordLabel: 'Mostrar contraseña',
  hidePasswordLabel: 'Ocultar contraseña',
  errors: {
    required: 'Completa este campo.',
    email: 'Introduce un correo electrónico válido.',
    handle: 'Usa entre 3 y 30 letras, números, puntos o guiones bajos.',
    passwordRequirements:
      'La contraseña debe tener al menos 6 caracteres, una mayúscula y un número.',
    mismatch: 'Las contraseñas no coinciden.',
    invalidField: 'Revisa el valor de este campo.',
    invalidCredentials: 'El correo o la contraseña no son correctos.',
    conflict:
      'No se pudo crear la cuenta: los datos entran en conflicto con una cuenta existente.',
    validation: 'Revisa los datos del formulario.',
    forbidden: 'No se pudo validar la solicitud. Vuelve a intentarlo.',
    network:
      'No se ha podido conectar. Comprueba tu conexión y vuelve a intentarlo.',
    generic:
      'No se ha podido completar la operación. Vuelve a intentarlo más tarde.',
  },
} as const;
