$ErrorActionPreference = 'Stop'
$jar = "lib\junit-platform-console-standalone-1.9.2.jar"
$src = Get-ChildItem -Recurse src -Filter *.java | ForEach-Object { $_.FullName }
if (Test-Path out) { Remove-Item -Recurse -Force out }
New-Item -ItemType Directory out | Out-Null
javac -cp $jar -d out @src
Write-Output "build exit: $LASTEXITCODE"
