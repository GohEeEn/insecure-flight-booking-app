# inSecurity

This is the Flight Reservation Web Application repository for UCD BSc Computer Science module [COMP47660](https://sisweb.ucd.ie/usis/!W_HU_MENU.P_PUBLISH?p_tag=MODULE&MODULE=COMP47660) Secure Software Engineering

## Pre-requisites

- Java 8 or higher version (version 1.8+)
- Maven
- MySQL server

## How to run this program

1. Create a database schema on your MySQL server. let say `application`

    - With `MySQL Workbench` :

        ![Create a MySQL DB schema with MySQL Workbench](./img/1.1_MySQL_Workbench_create_a_db_schema.png)

    - With `MySQL Shell` :

        ```SQL
        CREATE SCHEMA IF NOT EXISTS application; -- application : <schema_name>
        ```

2. Go to `<project_root_directory>/src/main/resources/application.properties` to configure the application connection with your server credential :

    ```properties
    # schema_name=`application` in this example
    spring.datasource.url=jdbc:mysql://localhost:3306/<schema_name>
    spring.datasource.username=<your_user_name>
    spring.datasource.password=<your_user_password>
    ```

    (**Note** : *Make sure the user account of credential given used has the privilege to modify the schema*)

3. Open a terminal on the root directory of this project (ie. where this doc is stored)
4. Run the command `mvn clean install` to run tests and build the project
5. Run the command `mvn spring-boot:run` to run the Spring Boot application
6. Go to `http://localhost:8080` to open the web application (setting can be done in `application.properties`)

    ```properties
    # 8080 by default
    server.port=<open_port>
    ```

7. The home page be like :

### Authors (Team inSecurity)

- [Ee En Goh](https://github.com/GohEeEn)
- [Seán Conor McLoughlin](https://github.com/SeanConor)
- [Olanipekun Akintola](https://github.com/olaakintola)
- [Svetoslav Nizhnichenkov](https://github.com/nizhnichenkov)