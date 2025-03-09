// config.js
//
// ENHANCED VERSION - WITH ISAAC ENCRYPTION SYNCHRONIZATION
// Successfully captures client's real ISAAC encryption to send valid packets
//
// Do not load "nashorn:mozilla_compat.js" since we're using a standalone Nashorn 
// that supports Java > 15.

// Import our needed classes using Java.type:
var RuneTekFourTransformer = Java.type('org.silabsoft.runeagent.transformer.RuneTekFourTransformer');
var RuneTekFourClientTransformer = Java.type('org.silabsoft.runeagent.transformer.RuneTekFourClientTransformer');
var ClassModifier           = Java.type('org.silabsoft.runeagent.util.ClassModifier');
var MethodWrapper           = Java.type('org.silabsoft.runeagent.util.MethodWrapper');

// Assuming "runeAgent" is provided globally by the runtime.
var transformer = new RuneTekFourTransformer(runeAgent.getAgent());

// Create and configure our ClassModifier for the ByteStream class.
var byteStream = new ClassModifier("ByteStream", "com.osroyale.Buffer", "org.silabsoft.runeagent.hook.GenericByteStream");
byteStream.addMethodWrapper(new MethodWrapper("p1isaac", "writeOpcode", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("p4", "writeDWord", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("p1", "writeByte", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("p2", "writeShort", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("p3", "writeDWordBigEndian", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("ip4", "method403", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("p8", "writeQWord", "(J)V", true));
byteStream.addMethodWrapper(new MethodWrapper("pjstr", "writeString", "(Ljava/lang/String;)V", true));
byteStream.addMethodWrapper(new MethodWrapper("np1", "writeNegatedByte", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("sp1", "method425", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("ip2", "writeLEShort", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("sp2", "writeShortA", "(I)V", true));
byteStream.addMethodWrapper(new MethodWrapper("isp2", "writeLEShortA", "(I)V", true));
byteStream.setReturnClassName("org.silabsoft.runeagent.transformer.RuneTekFourTransformer");

// Create our client transformer.
var clientModifier = new RuneTekFourClientTransformer("GameClient", "com.osroyale.Client", "org.silabsoft.runeagent.hook.GenericGameClient", "ISAACRandomGen");

// Add our modifiers to the transformer.
transformer.addClassModifier(byteStream);
transformer.addClassModifier(clientModifier);

// Finally, register our transformer with the agent.
runeAgent.addTransformer(transformer);

// Create a global stream variable for packet execution with encryption
var isaac = new (Java.type('com.osroyale.ISAACRandomGen'))([0,0,0,0]); // Initialize with default seeds
var stream = Java.type('com.osroyale.Buffer').create();
stream.encryption = isaac;

// Register the stream with RuneAgent's script engine
RuneTekFourTransformer.setOutStream(stream);

// Declare at module scope, initialized to null
var reflectionAccess = null;

// Add reflection utility for direct access in edge cases
function setupReflectionAccess() {
    try {
        var ClientClass = Java.type('com.osroyale.Client');
        var FieldClass = Java.type('java.lang.reflect.Field');
        
        // Get Client instance field if it exists
        var instanceField = ClientClass.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        var clientInstance = instanceField.get(null);  // null for static field
        
        // Try to access outgoing buffer
        var outgoingField = ClientClass.class.getDeclaredField("outgoing");
        outgoingField.setAccessible(true);
        var outgoingBuffer = outgoingField.get(clientInstance);
        
        // Try to access encryption field directly
        var encryptionField = null;
        var clientEncryption = null;
        try {
            // Field name may vary - try common names
            encryptionField = outgoingBuffer.getClass().getDeclaredField("encryption");
            encryptionField.setAccessible(true);
            clientEncryption = encryptionField.get(outgoingBuffer);
            RuneTekFourTransformer.log("Found client's ISAAC encryption: " + clientEncryption);
            
            // Update our global isaac reference with the client's actual encryption
            isaac = clientEncryption;
            RuneTekFourTransformer.log("Updated global ISAAC reference with client's encryption");
            
            // Update our global stream with the client's outgoing buffer
            stream = outgoingBuffer;
            RuneTekFourTransformer.log("Updated global stream with client's outgoing buffer");
            
            // Re-register the updated stream with RuneAgent
            RuneTekFourTransformer.setOutStream(stream);
            RuneTekFourTransformer.log("Re-registered client's stream with RuneAgent");
        } catch(e) {
            RuneTekFourTransformer.log("Couldn't access encryption field: " + e);
        }
        
        // Set the module-level variable
        reflectionAccess = {
            clientInstance: clientInstance,
            outgoingBuffer: outgoingBuffer,
            encryptionField: encryptionField,
            clientEncryption: clientEncryption
        };
        
        RuneTekFourTransformer.log("Reflection access setup completed");
    } catch(e) {
        RuneTekFourTransformer.log("Reflection setup failed: " + e);
    }
}

// Add function to inject packet using reflection as fallback
function injectPacketReflective(opcode, writePacketFunc) {
    try {
        if (!reflectionAccess || !reflectionAccess.outgoingBuffer) {
            RuneTekFourTransformer.log("Reflection access not available");
            return false;
        }

        var buffer = reflectionAccess.outgoingBuffer;
        
        RuneTekFourTransformer.log("Starting packet injection via reflection for opcode: " + opcode);
        
        // Get current buffer position for diagnostics
        var posField = buffer.getClass().getDeclaredField("position");
        posField.setAccessible(true);
        var originalPos = posField.getInt(buffer);
        RuneTekFourTransformer.log("Original buffer position: " + originalPos);

        // Try various method names for sending packets
        var methodsToTry = [
            { name: "writeOpcode", args: [opcode] },
            { name: "p1isaac", args: [opcode] },
            { name: "sendPacket", args: [opcode] }
        ];
        
        var writeSuccess = false;
        for (var i = 0; i < methodsToTry.length; i++) {
            try {
                var methodInfo = methodsToTry[i];
                var method = buffer.getClass().getDeclaredMethod(methodInfo.name, Java.type('int').class);
                method.setAccessible(true);
                method.invoke(buffer, methodInfo.args[0]);
                RuneTekFourTransformer.log("Successfully wrote opcode using method: " + methodInfo.name);
                writeSuccess = true;
                break;
            } catch (methodError) {
                RuneTekFourTransformer.log("Method " + methodsToTry[i].name + " failed: " + methodError);
            }
        }
        
        if (!writeSuccess) {
            RuneTekFourTransformer.log("Failed to write opcode with any method, attempting direct buffer manipulation");
            // Last resort: try to directly manipulate the buffer if all else fails
            try {
                var dataField = buffer.getClass().getDeclaredField("payload");
                dataField.setAccessible(true);
                var data = dataField.get(buffer);
                
                // Check if we can directly access the buffer's data array
                if (data) {
                    // Increment position
                    var newPos = originalPos + 1;
                    posField.setInt(buffer, newPos);
                    RuneTekFourTransformer.log("Directly updated buffer position to: " + newPos);
                }
            } catch (directError) {
                RuneTekFourTransformer.log("Direct buffer manipulation failed: " + directError);
                return false;
            }
        }

        // Let caller write packet data
        writePacketFunc(buffer);
        
        // Get final buffer position for diagnostics
        var finalPos = posField.getInt(buffer);
        RuneTekFourTransformer.log("Final buffer position after packet data: " + finalPos + " (wrote " + (finalPos - originalPos) + " bytes)");

        // Attempt to send by trying multiple possible flush methods
        var flushMethodsToTry = [
            "flushOutgoing",
            "sendOutgoingPacket", 
            "queuePacket",
            "flush",
            "sendPacket"
        ];
        
        var flushSuccess = false;
        for (var j = 0; j < flushMethodsToTry.length; j++) {
            try {
                var methodName = flushMethodsToTry[j];
                var flushMethod = reflectionAccess.clientInstance.getClass().getDeclaredMethod(methodName);
                flushMethod.setAccessible(true);
                flushMethod.invoke(reflectionAccess.clientInstance);
                RuneTekFourTransformer.log("Successfully flushed packet using method: " + methodName);
                flushSuccess = true;
                break;
            } catch (flushError) {
                RuneTekFourTransformer.log("Flush method " + flushMethodsToTry[j] + " failed: " + flushError);
            }
        }
        
        if (!flushSuccess) {
            RuneTekFourTransformer.log("Failed to flush packet with any method, trying network socket directly");
            // Try to find and flush the socket directly
            try {
                var socketField = reflectionAccess.clientInstance.getClass().getDeclaredField("socketStream");
                socketField.setAccessible(true);
                var socket = socketField.get(reflectionAccess.clientInstance);
                
                if (socket) {
                    var queueBytesMethod = socket.getClass().getDeclaredMethod("queueBytes", Java.type('int').class, Java.type('byte[]').class);
                    queueBytesMethod.setAccessible(true);
                    
                    // Try to get the buffer's data array
                    var dataField = buffer.getClass().getDeclaredField("payload");
                    dataField.setAccessible(true);
                    var data = dataField.get(buffer);
                    
                    if (data) {
                        queueBytesMethod.invoke(socket, finalPos, data);
                        RuneTekFourTransformer.log("Directly queued bytes to socket");
                        flushSuccess = true;
                    }
                }
            } catch (socketError) {
                RuneTekFourTransformer.log("Direct socket access failed: " + socketError);
            }
        }
        
        if (flushSuccess) {
            return true;
        } else {
            // Reset position on failure
            posField.setInt(buffer, originalPos);
            return false;
        }
    } catch (e) {
        RuneTekFourTransformer.log("Reflection packet injection failed: " + e);
        return false;
    }
}

// Create a polling mechanism that tries to connect periodically
function setupReflectionPoller() {
    var Thread = Java.type('java.lang.Thread');
    var AtomicBoolean = Java.type('java.util.concurrent.atomic.AtomicBoolean');
    
    var setupComplete = new AtomicBoolean(false);
    
    new Thread(function() {
        var attempts = 0;
        while (!setupComplete.get() && attempts < 30) { // 30 attempts = 30 seconds max
            attempts++;
            try {
                var ClientClass = Java.type('com.osroyale.Client');
                
                // Try to get instance - it might be null early in loading
                var instanceField = ClientClass.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                var clientInstance = instanceField.get(null);
                
                if (clientInstance != null) {
                    RuneTekFourTransformer.log("Client instance found on attempt " + attempts);
                    setupReflectionAccess();
                    setupComplete.set(true);
                } else {
                    RuneTekFourTransformer.log("Client instance not yet available (attempt " + attempts + ")");
                }
            } catch(e) {
                RuneTekFourTransformer.log("Reflection setup attempt " + attempts + " failed: " + e);
            }
            
            if (!setupComplete.get()) {
                Thread.sleep(1000); // Wait 1 second between attempts
            }
        }
    }).start();
}

// Start the poller immediately
setupReflectionPoller();
