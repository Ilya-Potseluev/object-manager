## Авторы

-   Поцелуев Илья (23-ИВТ-3)
-   Новиков Кирилл (23-ИВТ-3)

# Object Manager

Консольное приложение (CLI) для управления простыми таблицами данных, хранящимися в JSON файлах.

## Основные возможности:

-   Создание новых таблиц (схем) с помощью команды `create`.
-   Добавление данных (объектов) в таблицы с помощью команды `insert`.
-   Просмотр списка таблиц (`list`).
-   Отображение содержимого таблицы (`show`).
-   Выборка данных из таблицы с фильтрацией (`select`).
-   Просмотр справки по командам (`help`).

## Запуск

java -cp "target/classes;lib/gson-2.10.1.jar" objectmanager.Main data

## Примеры команд

-   **Создать таблицу пользователей:**
    ```
    create users {"description": "User table", "fields": {"id": "string", "username": "string", "email": "string", "role": "string"}}
    ```
-   **Добавить пользователей:**
    ```
    insert users {"id":"1","username":"admin","email":"admin@example.com","role":"admin"}
    insert users {"id":"2","username":"ivan","email":"ivan@example.com","role":"user"}
    insert users {"id":"3","username":"maria","email":"maria@example.com","role":"user"}
    ```
-   **Показать список таблиц:**
    ```
    list
    ```
-   **Показать всех пользователей:**
    ```
    show users
    ```
-   **Выбрать администраторов:**
    ```
    select users where role = admin
    ```
-   **Выход:**
    ```
    exit
    ```
