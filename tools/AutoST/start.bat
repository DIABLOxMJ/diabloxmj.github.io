@echo off
:: Se positionne automatiquement dans le dossier où se trouve ce fichier .bat
cd /d "%~dp0"

echo [1/3] Demarrage du serveur local Python (IA)...
:: Lance Python en arrière-plan sans bloquer le script
start "" py app.py

:: Petite pause pour laisser Flask s'initialiser tranquillement
timeout /t 4 /nobreak >nul

echo [2/3] Ouverture de l'interface graphique...
:: Ouvre l'interface HTML avec ton navigateur par défaut
start "" index.html

echo [3/3] Application prete !
echo Vous pouvez reduire cette fenetre, mais ne la fermez pas tant que vous utilisez l'application.
pause