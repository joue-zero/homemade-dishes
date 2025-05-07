@echo off
echo Checking required services...

REM Check MySQL
echo Checking MySQL...
sc query MySQL80 > nul
if %errorlevel% equ 0 (
    echo MySQL is running
) else (
    echo Starting MySQL...
    net start MySQL80
)

REM Check RabbitMQ
echo Checking RabbitMQ...
sc query RabbitMQ > nul
if %errorlevel% equ 0 (
    echo RabbitMQ is running
) else (
    echo Starting RabbitMQ...
    net start RabbitMQ
)

echo.
echo All services are ready!
echo.
echo Press any key to start the application...
pause > nul 