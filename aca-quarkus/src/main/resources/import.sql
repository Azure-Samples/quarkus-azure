INSERT INTO todo(id, title, completed, ordering, url) VALUES (1, 'Introduction to Quarkus Todo App', false, 0, null);
INSERT INTO todo(id, title, completed, ordering, url) VALUES (2, 'Quarkus on Azure App Service', false, 1, 'https://learn.microsoft.com/en-us/azure/developer/java/eclipse-microprofile/deploy-microprofile-quarkus-java-app-with-maven-plugin');
INSERT INTO todo(id, title, completed, ordering, url) VALUES (3, 'Quarkus on Azure Container Apps', false, 2, 'https://learn.microsoft.com/en-us/training/modules/deploy-java-quarkus-azure-container-app-postgres/');
INSERT INTO todo(id, title, completed, ordering, url) VALUES (4, 'Quarkus on Azure Functions', false, 3, 'https://learn.microsoft.com/en-us/azure/azure-functions/functions-create-first-quarkus');
/*
 * https://tomnomnom.com/posts/postgresql-sequence-naming--how-it-works
 *
 * The easiest way to define a sequence is to create a table with a
 * field that uses the 'SERIAL' pseudo-datatype. Such fields are
 * actually of the integer datatype, but let PostgreSQL know to create
 * a sequence associated with that field, named thusly:
 * tablename_fieldname_seq
 *
 * In this case, the table name is todo, the field name is id.
 */
ALTER SEQUENCE todo_id_seq RESTART WITH 5;
