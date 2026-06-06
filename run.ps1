$ErrorActionPreference = 'Stop'
.\build.ps1
java -cp out adventure_game.GameApp
