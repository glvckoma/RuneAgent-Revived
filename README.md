# RuneAgent

A Revival of the RuneAgent Java app for intercepting and analyzing network traffic in RuneScape Private Server clients.

## Features

- **Packet Interception**: Capture and analyze incoming and outgoing packets
- **JavaScript Scripting**: Write and execute scripts to interact with the game client
- **Dark Mode**: Customizable UI with light and dark themes
- **Script Management**: Save, load, import, and export scripts
- **Multiple Client Support**: Works with various RuneScape Private Server client versions

## Installation

1. Download the latest release from the [Releases](https://github.com/glvckoma/RuneAgent-Revived/releases) page
2. Extract the ZIP file to a location of your choice
3. Run the application using the provided run.bat file

## Directory Structure

The RuneAgent-bld directory contains:

- **RuneAgent.jar**: The main application JAR file
- **client.jar**: The RuneScape client JAR file
- **run.bat**: Batch file to run the application
- **config.js**: Configuration script for RuneAgent
- **default-config/**: Directory containing default configuration files
- **lib/**: Directory containing required libraries
- **scripts/**: Directory containing example and user scripts

## Usage

### Running the Application

1. Ensure Java is installed and available in your system PATH
2. Double-click the run.bat file to start RuneAgent with the RuneScape client


### Writing Scripts

RuneAgent uses JavaScript for scripting. You can write scripts to:

- Analyze packet data
- Automate client interactions
- Monitor game events

### Managing Scripts

- **Save**: Save your current script to the scripts folder
- **Import/Export**: Share scripts with others
- **Load**: Load a previously saved script

## Building from Source

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Ant

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/glvckoma/RuneAgent-Revived.git
```

2. Navigate to the project directory:
```bash
cd RuneAgent-Revived
```

3. Build using Ant:
```bash
ant clean jar
```

4. The built runagent.JAR file will be in the `dist` directory

## Acknowledgments

- Original RuneAgent by Silabsoft
- RSyntaxTextArea for syntax highlighting
- ASM library for bytecode manipulation
