# Jam User Guide

## Introduction

Jam-DAQ is a Java-based Data Acquisition system designed for nuclear physics experiments.

## Getting Started

### Installation

1. Download the latest release from the project repository
2. Extract the archive to your preferred location
3. Run `./jam.sh` on Linux/macOS or `jam.cmd` on Windows

### Basic Workflow

1. **Load Data** - Open an existing event file or create a new one
2. **Define Parameters** - Set up parameters for your analysis
3. **Apply Gates** - Define gates to filter events
4. **View Histograms** - Visualize your processed data

## Features

### Online Sorting

Process data in real-time from connected detectors.

```
Default sorting configuration:
- Ring buffer size: 1024 events
- Update rate: 100 Hz
```

### Offline Analysis

Analyze previously recorded event files with full replay capability.

### Gate Management

Create complex logical gates:
- **Band Gates** - Define regions in 2D histograms
- **Polygon Gates** - Custom shaped regions
- **Slice Gates** - 1D region selection

### File Formats

Supported formats:
- `.evn` - Event files (custom format)
- `.hdf` - HDF5 archived histograms
- `.csv` - ASCII export format

## Troubleshooting

### Application won't start

Ensure Java 21+ is installed:
```bash
java -version
```

### Memory issues with large files

Increase heap size:
```bash
export JAM_OPTS=-Xmx4g
./jam.sh
```

## Command Reference

### Main Application

Run the main application:
```bash
mvn exec:java -Dexec.mainClass="jam.Main"
```

### Build from Source

```bash
mvn clean package
```

## Support

- **Documentation**: See the project README.md
- **Issues**: Report bugs on the project repository
- **License**: NCSA Open Source License (see LICENSE file)

## Version History

### Version 4.0.0

- Migrated from JavaHelp to JavaFX WebView
- Modernized to Java 21+
- Improved documentation in Markdown format
