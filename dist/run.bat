@echo off
"c:\program files\java\jdk-17\bin\java.exe" -noverify -cp "RuneAgent.jar;lib/bcel-6.3.jar;lib/rsyntax.jar;lib/nashorn-core-15.4.jar;client.jar" -javaagent:"RuneAgent.jar"=config.js -jar client.jar
pause
