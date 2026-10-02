#!/bin/bash

# Define las carpetas del árbol
folders=(
    "controller"
    "controller/webmodel"
    "domain"
    "domain/model"
    "domain/repository"
    "domain/service"
    "domain/service/impl"
    "domain/usecase"
    "domain/usecase/book"
    "domain/usecase/book/impl"
    "persistence"
    "persistence/dao"
    "persistence/dao/cache"
    "persistence/dao/cache/impl"
    "persistence/dao/db"
    "persistence/dao/db/jdbc"
    "persistence/dao/db/jdbc/mapper"
    "persistence/dao/db/jpa"
    "persistence/dao/db/jpa/entity"
    "persistence/dao/db/jpa/mapper"
    "persistence/dao/db/jpa/repository"
    "persistence/repository"
    "persistence/repository/impl"
)

# Crea las carpetas
for folder in "${folders[@]}"; do
    mkdir -p "$folder"
    echo "Carpeta creada: $folder"
done

# Crea .gitkeep en las carpetas que tienen exactamente una subcarpeta
for folder in "${folders[@]}"; do
    # Contar cuántas subcarpetas tiene la carpeta actual
    subfolders=("$folder"/*/)
    if [ ${#subfolders[@]} -eq 1 ]; then
        touch "$folder/.gitkeep"
        echo ".gitkeep creado en: $folder"
    fi
done

echo "Estructura de carpetas creada con éxito."