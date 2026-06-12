# Run this script as Administrator to allow incoming connections

Write-Host "Setting up Windows Firewall rules for HireSense..." -ForegroundColor Yellow
Write-Host ""

# Remove existing rules if they exist
Write-Host "Removing any existing rules..." -ForegroundColor Gray
netsh advfirewall firewall delete rule name="HireSense-Frontend" 2>$null
netsh advfirewall firewall delete rule name="HireSense-Backend" 2>$null
netsh advfirewall firewall delete rule name="HireSense-AI" 2>$null

# Add new rules
Write-Host "Adding firewall rules..." -ForegroundColor Gray

$rules = @(
    @{Name="HireSense-Frontend"; Port=5173},
    @{Name="HireSense-Backend"; Port=8080},
    @{Name="HireSense-AI"; Port=8000}
)

foreach ($rule in $rules) {
    $result = netsh advfirewall firewall add rule name="$($rule.Name)" dir=in action=allow protocol=TCP localport=$($rule.Port)
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Added rule for $($rule.Name) on port $($rule.Port)" -ForegroundColor Green
    } else {
        Write-Host "❌ Failed to add rule for $($rule.Name)" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "Firewall setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Your website is now accessible at:" -ForegroundColor Yellow
Write-Host "http://10.1.0.228:5173" -ForegroundColor Cyan
Write-Host ""
Write-Host "Share this URL with friends on the same WiFi network!" -ForegroundColor Green
Write-Host ""

Pause
