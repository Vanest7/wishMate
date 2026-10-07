-- WishMate public handle transition
--
-- The user's local database already contains this change. Do not execute the
-- ALTER statement below against a database where username already exists.
-- New installations, or installations confirmed not to contain the column,
-- must apply:
--
-- ALTER TABLE usuarios
--   ADD COLUMN username VARCHAR(30)
--     CHARACTER SET ascii
--     COLLATE ascii_general_ci
--     NULL DEFAULT NULL
--     AFTER nombre,
--   ADD CONSTRAINT uq_usuarios_username UNIQUE (username);
--
-- These checks are read-only and should be run before choosing values for a
-- manual backfill. Existing accounts must not receive generated handles.
SELECT id, nombre, username
FROM usuarios
WHERE username IS NULL
   OR username = '';

SELECT id, username
FROM usuarios
WHERE username IS NOT NULL
  AND (
      CHAR_LENGTH(username) NOT BETWEEN 3 AND 30
      OR username REGEXP '[^a-z0-9._]'
      OR username <> LOWER(username)
  );

SELECT LOWER(username) AS normalized_username, COUNT(*) AS row_count
FROM usuarios
WHERE username IS NOT NULL
GROUP BY LOWER(username)
HAVING COUNT(*) > 1;

-- Choose each value explicitly and update by ID, for example:
-- UPDATE usuarios SET username = 'chosen.handle' WHERE id = 123;
-- Re-run the checks above after every backfill batch.
--
-- Do not execute the final tightening until every existing row has a valid
-- handle and every writer sends one. Preserve the current type, charset,
-- collation and constraint:
--
-- ALTER TABLE usuarios
--   MODIFY COLUMN username VARCHAR(30)
--     CHARACTER SET ascii
--     COLLATE ascii_general_ci
--     NOT NULL;
