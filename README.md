# cliligrane

Unofficial fork of [DossierFacile](https://github.com/MTES-MCT/dossierfacile-backend) keeping only the document watermarking feature and exposing it via CLI.

## Prerequisites

You need to have [JDK 21](https://openjdk.org/projects/jdk/21/) and [maven](https://maven.apache.org/) installed.

## Usage

1. Build with `mvn package -DskipTests`.
   The built jar will be in `cliligrane\target`
2. Run with `java -jar path/to/cliligrane.jar <input1> [<input2> ...] --output <output1> [<output2> ...] --text <watermark>`

## License

[MIT](https://choosealicense.com/licenses/mit/)
