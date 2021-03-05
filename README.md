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
    spring.datasource.url=jdbc:mysql://localhost:3306/<schema_name>?createDatabaseIfNotExist=true
    spring.datasource.username=<your_user_name>
    spring.datasource.password=<your_user_password>
    ```

    (**Note** : *Make sure the user account of credential given used has the privilege to modify the schema*)

3. Open a terminal on the root directory of this project (ie. where this doc is stored)
4. Run the command `mvn clean install` to run tests and build the project
5. Run the command `mvn spring-boot:run` to run the Spring Boot application
6. Go to `http://localhost:<server.port>` to open the web application (setting can be done in `application.properties`)

    ```properties
    # 8080 by default
    server.port=<open_port>
    ```

## Introduction to this web application

### Guest

Homepage is the first page you will always see when you open this application with `localhost:<server.port>`

![Homepage frontend for guest](img/Guest_1_Homepage.png)

1. Header of the navigation bar, also the link to homepage
2. Reservation retrieval form for any `guest` with their email address & reservation id
3. Club member registration link
4. Member login link
5. Flight search as a guest

### Member

1. Register an Account by clicking the `Register` link in the upper right-hand corner.
2. Input the required details and click `Register` at the bottom of the form.
3. Upon returning to the login page, you can login as the created Member via the `Login` button in the upper-right hand side of the page.
4. Clicking on the Member icon that has nor replaced the Register/Login icons, a dropdown menu will give you a choice of viewing reservations, your profile or logging out.
5. Click on Profile to see your personal details and find links to edit user details/password, view stored credit cards or delete the Member.
6. By default, credit cards are required to book a flight. Select `View Credit cards`, then `Add New Card` to create one. If you do not you will be propmpted to do so during the boooking process.
7. From the home page, select a Fight to book. An example might be From Sofia to Dublin on the 19th of March.
8. After completing a booking it will be available to view in `Reservations` in the Member drop-down menu in the upper-right hand side.
9. If more than 24 hours away, an option to cancel will be available.

### Authors (Team inSecurity)

- [Ee En Goh](https://github.com/GohEeEn)
- [Seán Conor McLoughlin](https://github.com/SeanConor)
- [Olanipekun Akintola](https://github.com/olaakintola)
- [Svetoslav Nizhnichenkov](https://github.com/nizhnichenkov)
