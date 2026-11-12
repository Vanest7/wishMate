-- Apply the handle migration after importing the repository's empty schema.
ALTER TABLE usuarios
  ADD COLUMN username VARCHAR(30)
    CHARACTER SET ascii COLLATE ascii_general_ci
    NULL DEFAULT NULL AFTER nombre,
  ADD CONSTRAINT uq_usuarios_username UNIQUE (username);
