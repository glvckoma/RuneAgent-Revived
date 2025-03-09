# RuneAgent

A Java agent for intercepting and analyzing network traffic in RuneScape clients.

## Features

- **Packet Interception**: Capture and analyze incoming and outgoing packets
- **JavaScript Scripting**: Write and execute scripts to interact with the game client
- **Dark Mode**: Customizable UI with light and dark themes
- **Script Management**: Save, load, import, and export scripts
- **Multiple Client Support**: Works with various RuneScape client versions

## Installation

1. Download the latest release from the [Releases](https://github.com/glvckoma/RuneAgent-Revived/releases) page
2. Extract the ZIP file to a location of your choice
3. Run the JAR file using Java:

```bash
java -jar RuneAgent.jar
```

## Usage

### Attaching to a Client

1. Start RuneAgent
2. Launch your RuneScape client
3. In RuneAgent, select the appropriate client version
4. Click "Attach" to connect to the client

### Writing Scripts

RuneAgent uses JavaScript for scripting. You can write scripts to:

- Analyze packet data
- Automate client interactions
- Monitor game events

Example script:

```javascript
// Log all incoming packets
stream.onPacket = function(packet) {
    console.log("Received packet: " + packet.opcode);
    return packet; // Return the packet to allow it to be processed
};
```

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

4. The built JAR file will be in the `dist` directory

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Original RuneAgent by Silabsoft
- RSyntaxTextArea for syntax highlighting
- ASM library for bytecode manipulation
