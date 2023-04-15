[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://img.shields.io/maven-central/v/io.maryk.lz4/lz4-android)](https://central.sonatype.com/artifact/io.maryk.lz4/lz4-android)

# LZ4 for android

LZ4 is a lossless data compression algorithm that is optimized for high compression
and fast decompression speed. This project exposes a statically compiled library of [LZ4](https://lz4.github.io/lz4/) for 
the Android platform. These dependencies can be added to projects that need to run LZ4 on Android devices.

## Installation

The project is published on Maven Central. To use LZ4 in your Android project, add the following dependency to your Gradle file:

Gradle:
```kts
implementation("io.maryk.lz4:lz4-android:1.9.4")
```

## Documentation
For more information on how to use LZ4 in your Android app, refer to the official [LZ4 documentation](https://lz4.github.io/lz4/).

## License
This project is licensed under the Apache License, Version 2.0 - see the [LICENSE file](LICENSE) for details.
