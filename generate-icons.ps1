Add-Type -AssemblyName System.Drawing

$srcPath = "c:\Users\nikas\Downloads\Perruquería canina\icon.png"
$src = [System.Drawing.Image]::FromFile($srcPath)

# Color de fondo celeste
$bgColor = [System.Drawing.Color]::FromArgb(255, 100, 181, 246)

$sizes = @{
    "mipmap-mdpi" = 48
    "mipmap-hdpi" = 72
    "mipmap-xhdpi" = 96
    "mipmap-xxhdpi" = 144
    "mipmap-xxxhdpi" = 192
}

foreach ($folder in $sizes.Keys) {
    $size = $sizes[$folder]
    $destFolder = "c:\Users\nikas\Downloads\Perruquería canina\androidNative\app\src\main\res\$folder"
    
    $bmp = New-Object System.Drawing.Bitmap($size, $size)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality
    $g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    
    $g.Clear($bgColor)
    
    $imgSize = [int]($size * 0.95)
    $offset = [int](($size - $imgSize) / 2)
    $g.DrawImage($src, $offset, $offset, $imgSize, $imgSize)
    
    $g.Dispose()
    
    $bmp.Save("$destFolder\ic_launcher.png", [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save("$destFolder\ic_launcher_round.png", [System.Drawing.Imaging.ImageFormat]::Png)
    
    $bmp.Dispose()
    Write-Host "Generado $folder - ${size}px"
}

$src.Dispose()
Write-Host "Iconos generados correctamente"
