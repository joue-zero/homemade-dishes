# Stop any running Java processes (Spring Boot applications)
Write-Host "Stopping any running services..." -ForegroundColor Yellow
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force

# Function to build and run a service
function BuildAndRunService {
    param (
        [string]$serviceName,
        [string]$port
    )
    
    Write-Host "`nBuilding $serviceName..." -ForegroundColor Cyan
    Set-Location $serviceName
    mvn clean install
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Starting $serviceName on port $port..." -ForegroundColor Green
        Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$serviceName'; mvn spring-boot:run"
        Start-Sleep -Seconds 5  # Wait for service to start
    } else {
        Write-Host "Failed to build $serviceName" -ForegroundColor Red
        exit 1
    }
    
    Set-Location ..
}

# Main execution
Write-Host "Starting rebuild and run process..." -ForegroundColor Yellow

# Build and run services in order
BuildAndRunService -serviceName "user-service" -port "8081"
BuildAndRunService -serviceName "dish-service" -port "8082"
BuildAndRunService -serviceName "order-service" -port "8084"

Write-Host "`nAll services have been rebuilt and started!" -ForegroundColor Green
Write-Host "Services are running on:"
Write-Host "- User Service: http://localhost:8081"
Write-Host "- Dish Service: http://localhost:8082"
Write-Host "- Order Service: http://localhost:8084" 