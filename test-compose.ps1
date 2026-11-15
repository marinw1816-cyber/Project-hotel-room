$base = "http://localhost:8080/api/rooms"
$tmp = Join-Path $env:TEMP "hotel-test"
New-Item -ItemType Directory -Force $tmp | Out-Null

'{"roomNumber": 201, "capacity": 2, "pricePerNight": 70.00}'  | Set-Content -Encoding ascii "$tmp\room.json"
'{"roomNumber": 201, "capacity": 4, "pricePerNight": 120.00}' | Set-Content -Encoding ascii "$tmp\room-update.json"
'{"roomNumber": 202, "capacity": 0, "pricePerNight": 70.00}'  | Set-Content -Encoding ascii "$tmp\room-bad.json"

# Wait until the app is ready (max. 60 s)
for ($i = 0; $i -lt 30; $i++) {
    $health = curl.exe -s http://localhost:8080/actuator/health
    if ($health -match '"status":"UP"') { break }
    Start-Sleep -Seconds 2
}
Write-Host "Health: $health"
Write-Host ""

$failed = 0
function Test-Call($label, $expected, [string[]]$curlArgs) {
    $code = curl.exe -s -o "$tmp\out.json" -w "%{http_code}" @curlArgs
    $body = Get-Content "$tmp\out.json" -Raw -ErrorAction SilentlyContinue
    if ($code -eq $expected) { $result = "OK  " } else { $result = "FAIL"; $script:failed++ }
    Write-Host "$result $label -> $code (expected $expected) $body"
}

$json = @("-H", "Content-Type: application/json")

# Remove room 201 from an earlier run, so the test can run again
curl.exe -s -o NUL -X DELETE "$base/201"

Test-Call "POST room 201"          "201" (@("-X", "POST", $base) + $json + @("--data", "@$tmp\room.json"))
Test-Call "POST room 201 again"    "409" (@("-X", "POST", $base) + $json + @("--data", "@$tmp\room.json"))
Test-Call "POST capacity 0"        "400" (@("-X", "POST", $base) + $json + @("--data", "@$tmp\room-bad.json"))
Test-Call "PUT room 201"           "200" (@("-X", "PUT", "$base/201") + $json + @("--data", "@$tmp\room-update.json"))
Test-Call "GET all rooms"          "200" @($base)
Test-Call "GET room 201"           "200" @("$base/201")
Test-Call "GET room 999"           "404" @("$base/999")
Test-Call "DELETE room 201"        "204" @("-X", "DELETE", "$base/201")
Test-Call "GET room 201 after DELETE" "404" @("$base/201")

Write-Host ""
if ($failed -eq 0) { Write-Host "All tests passed" } else { Write-Host "$failed test(s) failed"; exit 1 }