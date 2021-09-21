![Develop Status][workflow-badge-develop]
![Main Status][workflow-badge-main]
![Version][version-badge]

# db-version-tool
CLI tool to enable versioning and changes to a database

## Usage
```shell
java -jar DBVersionTool.jar
```

### Arguments
 - `-v | --version` - The version to migrate this database to. Defaults to the latest. (optional)
 - `-d | --dir` - The directory containing the sql scripts to execute.
 - `-f | --generate-file` - Filepath to create a file at, instead of directly updating the database. (optional)

### Environment variables
 - `DATABASE_URL` - The database url to update
 - `DATABASE_USERNAME` - The username for connecting to the database
 - `DATABASE_PASSWORD` - The password for connecting to the database


### Github Packages Authentication
Currently public packages on Github require authentication.
This project uses dependencies from Github packages, if building from source make sure the following environment variables are set:
 - `GH_USER` - Github username
 - `GH_TOKEN` - Github access token(any scope)

### Testing
Run unit tests with 
```shell
./gradlew test
```

### Building a runnable jar
Build a jar that can be run standalone with
```shell
./gradlew fatjar
```

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

New features, fixes, and bugs should be branched off of develop.

Please make sure to update tests as appropriate.

## License
[MIT][mit-license]

[workflow-badge-develop]: https://img.shields.io/github/workflow/status/lukecmstevens/db-version-lib/test/develop?label=develop
[workflow-badge-main]: https://img.shields.io/github/workflow/status/lukecmstevens/db-version-lib/release/main?label=main
[version-badge]: https://img.shields.io/github/v/release/lukecmstevens/db-version-lib
[mit-license]: https://choosealicense.com/licenses/mit/