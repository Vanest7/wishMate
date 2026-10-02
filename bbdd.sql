-- =========================================
-- Script de creación de base de datos
-- =========================================

CREATE DATABASE IF NOT EXISTS regalos_db;
USE regalos_db;

-- =========================================
-- Tabla de usuarios
-- =========================================
CREATE TABLE usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    mail VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- =========================================
-- Tabla de grupos
-- Solo guarda el nombre y el creador (opcional)
-- =========================================
CREATE TABLE grupos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_creador INT NOT NULL,
    FOREIGN KEY (id_creador) REFERENCES usuarios(id)
);

-- =========================================
-- Tabla intermedia: relación grupos - usuarios
-- Así podemos tener múltiples usuarios en un grupo
-- =========================================
CREATE TABLE grupo_usuarios (
    id_grupo INT NOT NULL,
    id_usuario INT NOT NULL,
    PRIMARY KEY (id_grupo, id_usuario),
    FOREIGN KEY (id_grupo) REFERENCES grupos(id),
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- =========================================
-- Lista de deseos
-- Cada usuario puede tener varios deseos
-- flag_cogido indica si otro usuario ya lo ha escogido como regalo
-- =========================================
CREATE TABLE lista_deseos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    flag_cogido BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);

-- =========================================
-- Regalos
-- Relaciona a un usuario que REGALA con un deseo de otro usuario
-- =========================================
CREATE TABLE regalos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario_regalador INT NOT NULL,
    id_deseo INT NOT NULL,
    fecha_regalo DATE DEFAULT CURRENT_DATE,
    FOREIGN KEY (id_usuario_regalador) REFERENCES usuarios(id),
    FOREIGN KEY (id_deseo) REFERENCES lista_deseos(id)
);

-- =========================================
-- Tabla de tokens JWT
-- =========================================
CREATE TABLE tokens (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario INT NOT NULL,
    token VARCHAR(500) NOT NULL,  -- se podría guardar en texto plano o hash
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_expiracion TIMESTAMP NOT NULL,
    valido BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
);
