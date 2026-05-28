$ErrorActionPreference = 'Stop'

$baseUrl = if ($env:RENTAL_GATEWAY_URL) { $env:RENTAL_GATEWAY_URL } else { 'http://127.0.0.1:8888' }
$stamp = Get-Date -Format 'yyyyMMddHHmmss'
$owner = @{
    username = "owner_$stamp"
    password = 'Passw0rd123'
    phone = "138$($stamp.Substring($stamp.Length - 8))"
    email = "owner_$stamp@example.com"
}
$tenant = @{
    username = "tenant_$stamp"
    password = 'Passw0rd123'
    phone = "139$($stamp.Substring($stamp.Length - 8))"
    email = "tenant_$stamp@example.com"
}

function Invoke-Json {
    param(
        [string]$Method,
        [string]$Path,
        [object]$Body = $null,
        [string]$Token = ''
    )

    $headers = @{}
    if ($Token) {
        $headers.Authorization = "Bearer $Token"
    }

    $uri = "$baseUrl$Path"
    if ($null -ne $Body) {
        $json = $Body | ConvertTo-Json -Depth 20
        return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -ContentType 'application/json' -Body $json -TimeoutSec 30
    }

    return Invoke-RestMethod -Method $Method -Uri $uri -Headers $headers -TimeoutSec 30
}

function Assert-True {
    param([bool]$Condition, [string]$Message)
    if (-not $Condition) {
        throw $Message
    }
}

Write-Host "Smoke test gateway: $baseUrl"

Write-Host 'Register owner'
Invoke-Json -Method 'Post' -Path '/api/user/register' -Body $owner | Out-Null

Write-Host 'Register tenant'
Invoke-Json -Method 'Post' -Path '/api/user/register' -Body $tenant | Out-Null

Write-Host 'Login owner'
$ownerLogin = Invoke-Json -Method 'Post' -Path '/api/user/login' -Body @{
    usernameOrEmailOrPhone = $owner.username
    password = $owner.password
}
$ownerToken = $ownerLogin.token
Assert-True ($ownerToken.Length -gt 20) 'Owner token was not returned.'

Write-Host 'Login tenant'
$tenantLogin = Invoke-Json -Method 'Post' -Path '/api/user/login' -Body @{
    usernameOrEmailOrPhone = $tenant.username
    password = $tenant.password
}
$tenantToken = $tenantLogin.token
Assert-True ($tenantToken.Length -gt 20) 'Tenant token was not returned.'

Write-Host 'Read owner profile'
$ownerInfo = Invoke-Json -Method 'Get' -Path '/api/user/info' -Token $ownerToken
Assert-True ($ownerInfo.id -gt 0) 'Owner info id was not returned.'

Write-Host 'Read tenant profile'
$tenantInfo = Invoke-Json -Method 'Get' -Path '/api/user/info' -Token $tenantToken
Assert-True ($tenantInfo.id -gt 0) 'Tenant info id was not returned.'

Write-Host 'Owner uploads a house'
$houseBody = @{
    title = "Smoke Test Apartment $stamp"
    description = 'A clean two-bedroom apartment used by the automated smoke test.'
    address = 'No. 18 Test Road'
    city = 'Guangzhou'
    district = 'Tianhe'
    price = 2600
    area = 72
    roomNum = 2
    toiletNum = 1
    floor = 8
    totalFloor = 22
    orientation = 'South'
    decoration = 'Modern'
    facilities = 'WiFi,Air Conditioner,Washer'
    imageUrls = @(
        'https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?auto=format&fit=crop&w=1200&q=80'
    )
}
Invoke-Json -Method 'Post' -Path '/api/house/upload' -Body $houseBody -Token $ownerToken | Out-Null

Write-Host 'Search uploaded house'
$search = Invoke-Json -Method 'Post' -Path '/api/house/search' -Body @{
    keyword = "Smoke Test Apartment $stamp"
    page = 1
    size = 5
} -Token $tenantToken
Assert-True ($search.records.Count -gt 0) 'Uploaded house was not found by search.'
$houseId = $search.records[0].id
Assert-True ($houseId -gt 0) 'House id was not returned.'

Write-Host 'Read house detail'
$detail = Invoke-Json -Method 'Get' -Path "/api/house/$houseId" -Token $tenantToken
Assert-True ($detail.id -eq $houseId) 'House detail id did not match.'

Write-Host 'Toggle favorite'
Invoke-Json -Method 'Post' -Path "/api/house/favorite/toggle?houseId=$houseId" -Token $tenantToken | Out-Null
$favorite = Invoke-Json -Method 'Get' -Path "/api/house/favorite/check?houseId=$houseId" -Token $tenantToken
Assert-True ([bool]$favorite) 'Favorite check did not return true.'

Write-Host 'Create order'
$orderNo = Invoke-Json -Method 'Post' -Path '/api/order/create' -Body @{
    houseId = $houseId
    startDate = '2026-06-01'
    endDate = '2026-09-01'
    deposit = 2600
} -Token $tenantToken
Assert-True ($orderNo.Length -gt 5) 'Order number was not returned.'

Write-Host 'Read my orders'
$orders = Invoke-Json -Method 'Post' -Path '/api/order/my' -Body @{ page = 1; size = 10 } -Token $tenantToken
Assert-True ($orders.records.Count -gt 0) 'My orders did not contain created order.'
$orderId = ($orders.records | Where-Object { $_.orderNo -eq $orderNo } | Select-Object -First 1).id
Assert-True ($orderId -gt 0) 'Created order id was not found.'

Write-Host 'Pay order'
Invoke-Json -Method 'Post' -Path "/api/order/pay/$orderId" -Token $tenantToken | Out-Null

Write-Host 'Add comment'
Invoke-Json -Method 'Post' -Path '/api/comment/add' -Body @{
    houseId = $houseId
    content = 'Smoke test comment: clean flow.'
    rating = 5
    imageUrls = @()
    parentId = $null
} -Token $tenantToken | Out-Null

Write-Host 'List comments'
$comments = Invoke-Json -Method 'Get' -Path "/api/comment/list/$houseId" -Token $tenantToken
Assert-True ($comments.Count -gt 0) 'Comment list is empty.'

Write-Host 'Send chat message'
Invoke-Json -Method 'Post' -Path '/api/message/chat' -Body @{
    toUserId = $ownerInfo.id
    content = 'Smoke test chat message.'
} -Token $tenantToken | Out-Null

Write-Host 'Read chat history'
$chat = Invoke-Json -Method 'Get' -Path "/api/message/chat/$($tenantInfo.id)" -Token $ownerToken
Assert-True ($chat.Count -gt 0) 'Chat history is empty.'

Write-Host ''
Write-Host 'SMOKE TEST PASSED'
Write-Host "Owner: $($owner.username)"
Write-Host "Tenant: $($tenant.username)"
Write-Host "HouseId: $houseId"
Write-Host "OrderNo: $orderNo"
