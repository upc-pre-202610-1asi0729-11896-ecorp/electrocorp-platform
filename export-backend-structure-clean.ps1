$ErrorActionPreference = "Stop"

$output = "backend-structure-clean.txt"
$outputPath = Join-Path (Get-Location) $output

if (Test-Path $outputPath) {
  Remove-Item $outputPath -Force
}

$excludedFolders = @(
  "\target\",
  "\.idea\",
  "\.vscode\",
  "\.git\",
  "\.mvn\",
  "\build\",
  "\out\"
)

function Is-ExcludedPath {
  param ([string]$path)

  foreach ($folder in $excludedFolders) {
    if ($path -like "*$folder*") {
      return $true
    }
  }

  return $false
}

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$writer = [System.IO.StreamWriter]::new($outputPath, $false, $utf8NoBom)

try {
  $writer.WriteLine("============================================================")
  $writer.WriteLine("ELECTROCORP BACKEND CLEAN STRUCTURE EXPORT")
  $writer.WriteLine("Generated at: $(Get-Date)")
  $writer.WriteLine("Root: $(Get-Location)")
  $writer.WriteLine("============================================================")
  $writer.WriteLine("")

  Get-ChildItem -Path . -Recurse |
  Where-Object {
    $_.FullName -ne $outputPath -and
    -not (Is-ExcludedPath $_.FullName)
  } |
  Sort-Object FullName |
  ForEach-Object {
    $relativePath = $_.FullName.Replace((Get-Location).Path + "\", "")

    if ($_.PSIsContainer) {
      $writer.WriteLine("[DIR]  $relativePath")
    } else {
      $writer.WriteLine("[FILE] $relativePath")
    }
  }
}
finally {
  $writer.Dispose()
}

Write-Host "Backend clean structure exported to $output"