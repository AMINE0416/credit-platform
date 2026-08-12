@echo off
REM ===============================================
REM Script : Compiler et Demarrer l'Application
REM ===============================================

echo ========================================
echo   Compilation et Demarrage
echo ========================================
echo.

cd /d "c:\Users\asus\Documents\credit-platform"

echo Repertoire actuel : %CD%
echo.

REM Etape 1 : Clean et Install
echo [1/2] Nettoyage et compilation...
echo Commande : mvn clean install -DskipTests
echo.

call mvn clean install -DskipTests

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERREUR] La compilation a echoue !
    echo Verifiez les erreurs ci-dessus.
    echo.
    pause
    exit /b 1
)

echo.
echo [OK] Compilation reussie !
echo.

REM Etape 2 : Demarrer l'application
echo [2/2] Demarrage de l'application...
echo Commande : mvn spring-boot:run
echo.
echo L'application va demarrer...
echo Pour arreter, appuyez sur Ctrl+C
echo.
echo ========================================
echo.

call mvn spring-boot:run
