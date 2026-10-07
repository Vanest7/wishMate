import nx from '@nx/eslint-plugin';
import baseConfig from '../../eslint.config.mjs';

const featureNames = [
  'auth',
  'dashboard',
  'groups',
  'events',
  'wishes',
  'reservations',
];
const generatedInternals = {
  group: ['**/api-client/src/lib/generated/**'],
  message: 'Import the generated client through @wishmate/api-client.',
};

export default [
  ...nx.configs['flat/angular'],
  ...nx.configs['flat/angular-template'],
  ...baseConfig,
  {
    files: ['**/*.ts'],
    rules: {
      '@angular-eslint/directive-selector': [
        'error',
        {
          type: 'attribute',
          prefix: 'app',
          style: 'camelCase',
        },
      ],
      '@angular-eslint/component-selector': [
        'error',
        {
          type: 'element',
          prefix: 'app',
          style: 'kebab-case',
        },
      ],
    },
  },
  {
    files: ['**/*.html'],
    // Override or add rules here
    rules: {},
  },
  ...featureNames.map((feature) => ({
    files: [`**/features/${feature}/**/*.ts`],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            generatedInternals,
            {
              regex: `^(?:\\.\\./)+(?:features/)?(?:${featureNames.filter((name) => name !== feature).join('|')})/`,
              message:
                'Features must not import another feature internals. Use routes or a narrowly shared capability.',
            },
          ],
        },
      ],
    },
  })),
  {
    files: ['**/core/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            generatedInternals,
            {
              group: ['**/features/**', '**/layout/**'],
              message: 'Core must not depend on feature or layout components.',
            },
          ],
        },
      ],
    },
  },
  {
    files: ['**/shared/ui/**/*.ts'],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          paths: [
            {
              name: '@wishmate/api-client',
              message: 'Shared UI must be domain-neutral.',
            },
          ],
          patterns: [
            {
              group: [
                '**/features/**',
                '**/core/**',
                '**/domain/**',
                '**/api-client/**',
              ],
              message:
                'Shared UI receives inputs and emits events; it must not access domain or application services.',
            },
          ],
        },
      ],
    },
  },
  ...featureNames.map((feature) => ({
    files: [`**/features/${feature}/data-access/**/*.ts`],
    rules: {
      'no-restricted-imports': [
        'error',
        {
          patterns: [
            generatedInternals,
            {
              regex: `^(?:\\.\\./)+(?:features/)?(?:${featureNames.filter((name) => name !== feature).join('|')})/`,
              message: 'Do not depend on another feature internals.',
            },
            {
              group: ['**/pages/**', '**/ui/**', '**/layout/**'],
              message: 'Data access must not depend on presentation.',
            },
          ],
        },
      ],
    },
  })),
];
