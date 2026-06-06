$ErrorActionPreference = 'Stop'
.\build.ps1
$jar = "lib\junit-platform-console-standalone-1.9.2.jar"
java -jar $jar -cp out --scan-classpath --disable-banner
