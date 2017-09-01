CREATE USER udiia WITH PASSWORD 'diia';
CREATE DATABASE diia OWNER udiia;
\c diia

CREATE TABLE escuela(
    id_escuela TEXT PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    direccion TEXT NOT NULL,
    ciudad TEXT NOT NULL
);

CREATE TABLE docente(
    id_docente TEXT PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    sexo TEXT NOT NULL,
    estado_civil TEXT NOT NULL,
    fecha_nacimiento TEXT NOT NULL,
    id_facebook TEXT,
    id_crea TEXT
);

CREATE TABLE escueladocente(
    id_docente TEXT REFERENCES docente (id_docente) NOT NULL,
    id_escuela TEXT REFERENCES escuela (id_escuela) NOT NULL,
    PRIMARY KEY (id_docente, id_escuela)
);

CREATE TABLE curso(
    id_curso TEXT PRIMARY KEY NOT NULL,
    titulo TEXT NOT NULL,
    codigo TEXT NOT NULL,
    periodo TEXT NOT NULL,
    seccion TEXT NOT NULL,
    id_docente TEXT REFERENCES docente (id_docente) NOT NULL,
    id_escuela TEXT REFERENCES escuela (id_escuela) NOT NULL,
    id_facebook TEXT,
    id_crea TEXT
);

CREATE TABLE alumno(
    id_alumno TEXT PRIMARY KEY NOT NULL,
    nombre TEXT NOT NULL,
    apellido TEXT NOT NULL,
    sexo TEXT NOT NULL,
    fecha_nacimiento TEXT NOT NULL,
    perfil_academico TEXT NOT NULL,
    especialidad TEXT NOT NULL,
    email TEXT NOT NULL,
    telefono TEXT NOT NULL,
    id_facebook TEXT,
    id_crea TEXT
);

CREATE TABLE alumnocurso(
    id_alumno TEXT REFERENCES alumno (id_alumno) NOT NULL,
    id_curso TEXT REFERENCES curso (id_curso) NOT NULL,
    PRIMARY KEY (id_alumno, id_curso)
);

CREATE TABLE material(
    id_material TEXT PRIMARY KEY NOT NULL,
    id_curso TEXT REFERENCES curso (id_curso) NOT NULL,
    tipo TEXT NOT NULL,
    fecha_creacion TEXT,
    contenido TEXT NOT NULL,
    url_ubicacion TEXT NOT NULL
);

CREATE TABLE materialalumno(
    id_material TEXT PRIMARY KEY NOT NULL,
    id_alumno TEXT REFERENCES alumno (id_alumno) NOT NULL,
    id_curso TEXT REFERENCES curso (id_curso) NOT NULL,
    fecha_creacion TEXT,
    contenido TEXT NOT NULL,
    url_ubicacion TEXT NOT NULL
);

