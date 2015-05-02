CREATE extension IF NOT EXISTS ltree;
CREATE TABLE departments (
    id int NOT NULL PRIMARY KEY,
    name varchar(255) NOT NULL,
    struct ltree NOT NULL,
    deleted_at TIMESTAMP
);
CREATE UNIQUE INDEX departments_struct_idx ON departments (struct);
CREATE TABLE user_department(
    user_id int NOT NULL,
    department_id int NOT NULL,
    PRIMARY KEY(user_id, department_id)
);