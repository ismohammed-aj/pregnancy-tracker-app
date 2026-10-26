param(
    [string]$OutputRoot = "",
    [string]$SeedSqlPath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

Add-Type -AssemblyName System.Drawing

if ([string]::IsNullOrWhiteSpace($OutputRoot)) {
    $OutputRoot = Join-Path $PSScriptRoot "..\src\main\resources\static\baby-development-images"
}

if ([string]::IsNullOrWhiteSpace($SeedSqlPath)) {
    $SeedSqlPath = Join-Path $PSScriptRoot "..\src\main\resources\db\baby_development_image_seed.sql"
}

$FruitComparisons = @(
    "tiny seed",
    "sesame seed",
    "chia seed",
    "poppy seed",
    "apple seed",
    "sweet pea",
    "blueberry",
    "raspberry",
    "cherry",
    "strawberry",
    "fig",
    "lime",
    "plum",
    "lemon",
    "apple",
    "avocado",
    "pear",
    "bell pepper",
    "mango",
    "banana",
    "carrot",
    "papaya",
    "grapefruit",
    "corn",
    "rutabaga",
    "zucchini",
    "cauliflower",
    "eggplant",
    "butternut squash",
    "cabbage",
    "coconut",
    "squash",
    "pineapple",
    "cantaloupe",
    "honeydew melon",
    "melon",
    "romaine lettuce",
    "leek",
    "mini watermelon",
    "watermelon"
)

$AnimalComparisons = @(
    "tiny ladybug dot",
    "ladybug",
    "honeybee",
    "small butterfly",
    "hummingbird egg",
    "baby snail",
    "small seahorse",
    "tiny frog",
    "field mouse",
    "small hamster",
    "young lizard",
    "small sparrow",
    "guinea pig",
    "baby hedgehog",
    "kitten paw",
    "small kitten",
    "small rabbit",
    "little duckling",
    "small puppy",
    "tiny turtle",
    "baby ferret",
    "young kitten",
    "small otter pup",
    "newborn lamb hoof",
    "small meerkat",
    "little penguin",
    "baby rabbit",
    "small fox cub",
    "young cat",
    "small puppy",
    "young hare",
    "small koala",
    "baby seal pup",
    "small lamb",
    "young beagle",
    "small terrier",
    "newborn fawn",
    "small cat",
    "newborn goat kid",
    "newborn lamb"
)

$ObjectComparisons = @(
    "pinhead",
    "tiny bead",
    "grain of rice",
    "shirt button",
    "small pearl",
    "ring stone",
    "bottle cap",
    "large coin",
    "key fob",
    "computer mouse",
    "tennis ball",
    "mug",
    "compact camera",
    "glasses case",
    "small book",
    "banana case",
    "TV remote",
    "water bottle",
    "small pouch",
    "corn cob",
    "lunch box",
    "travel umbrella",
    "small bowl",
    "mini tablet",
    "loaf of bread",
    "cushion",
    "small backpack",
    "rolled towel",
    "small basket",
    "folded blanket",
    "newborn onesie",
    "soft pillow",
    "diaper pack",
    "baby blanket",
    "crib pillow",
    "swaddle blanket",
    "hospital bag",
    "baby carrier",
    "small watermelon",
    "pumpkin"
)

$Modes = @(
    @{
        Key = "fruit"
        Label = "Fruit"
        Items = $FruitComparisons
        Accent = "#D6336C"
        Light = "#FFF0F6"
        Shape = "fruit"
    },
    @{
        Key = "animal"
        Label = "Animal"
        Items = $AnimalComparisons
        Accent = "#21BDB4"
        Light = "#EFFAF8"
        Shape = "animal"
    },
    @{
        Key = "object"
        Label = "Object"
        Items = $ObjectComparisons
        Accent = "#8E6BE8"
        Light = "#F3EFFF"
        Shape = "object"
    }
)

function Get-Slug {
    param([string]$Name)

    $slug = $Name.ToLowerInvariant()
    $slug = $slug -replace "^(a|an|the)\s+", ""
    $slug = $slug -replace "'s", "s"
    $slug = $slug -replace "[^a-z0-9]+", "_"
    return $slug.Trim("_")
}

function Convert-Color {
    param([string]$Hex)
    return [System.Drawing.ColorTranslator]::FromHtml($Hex)
}

function New-RoundedPath {
    param(
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height,
        [float]$Radius
    )

    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $diameter = $Radius * 2
    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()
    return $path
}

function Draw-CenteredText {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Text,
        [System.Drawing.RectangleF]$Bounds,
        [System.Drawing.Font]$Font,
        [System.Drawing.Brush]$Brush
    )

    $format = [System.Drawing.StringFormat]::new()
    $format.Alignment = [System.Drawing.StringAlignment]::Center
    $format.LineAlignment = [System.Drawing.StringAlignment]::Center
    $format.Trimming = [System.Drawing.StringTrimming]::EllipsisWord
    $Graphics.DrawString($Text, $Font, $Brush, $Bounds, $format)
    $format.Dispose()
}

function Draw-Leaf {
    param(
        [System.Drawing.Graphics]$Graphics,
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height
    )

    $brush = [System.Drawing.SolidBrush]::new((Convert-Color "#51B788"))
    $path = [System.Drawing.Drawing2D.GraphicsPath]::new()
    $path.AddBezier($X, $Y + $Height, $X + $Width * 0.1, $Y, $X + $Width * 0.9, $Y, $X + $Width, $Y + $Height)
    $path.AddBezier($X + $Width, $Y + $Height, $X + $Width * 0.65, $Y + $Height * 0.65, $X + $Width * 0.35, $Y + $Height * 0.65, $X, $Y + $Height)
    $Graphics.FillPath($brush, $path)
    $path.Dispose()
    $brush.Dispose()
}

function Draw-FruitIcon {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Name,
        [System.Drawing.Color]$Accent
    )

    $centerX = 384
    $centerY = 356
    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(30, 47, 36, 48))
    $Graphics.FillEllipse($shadow, 210, 525, 348, 42)
    $shadow.Dispose()

    if ($Name -match "seed|chia|sesame|poppy") {
        $brush = [System.Drawing.SolidBrush]::new((Convert-Color "#6E4E3A"))
        for ($i = 0; $i -lt 7; $i++) {
            $x = 285 + (($i % 4) * 54)
            $y = 310 + ([math]::Floor($i / 4) * 58)
            $Graphics.FillEllipse($brush, $x, $y, 44, 58)
        }
        $brush.Dispose()
        return
    }

    if ($Name -match "banana") {
        $pen = [System.Drawing.Pen]::new((Convert-Color "#F6C453"), 48)
        $pen.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
        $pen.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
        $Graphics.DrawArc($pen, 235, 250, 315, 215, 20, 135)
        $pen.Dispose()
        return
    }

    if ($Name -match "corn") {
        $cob = [System.Drawing.SolidBrush]::new((Convert-Color "#F3C84B"))
        $Graphics.FillEllipse($cob, 286, 214, 196, 286)
        $cob.Dispose()
        $kernel = [System.Drawing.SolidBrush]::new((Convert-Color "#FFE58F"))
        for ($row = 0; $row -lt 7; $row++) {
            for ($col = 0; $col -lt 4; $col++) {
                $Graphics.FillEllipse($kernel, 322 + $col * 35, 252 + $row * 31, 18, 18)
            }
        }
        $kernel.Dispose()
        Draw-Leaf $Graphics 236 356 128 164
        Draw-Leaf $Graphics 405 356 128 164
        return
    }

    if ($Name -match "avocado") {
        $outer = [System.Drawing.SolidBrush]::new((Convert-Color "#5AA469"))
        $inner = [System.Drawing.SolidBrush]::new((Convert-Color "#CFE8A9"))
        $pit = [System.Drawing.SolidBrush]::new((Convert-Color "#9B6A3D"))
        $Graphics.FillEllipse($outer, 250, 188, 268, 330)
        $Graphics.FillEllipse($inner, 292, 226, 184, 254)
        $Graphics.FillEllipse($pit, 338, 338, 92, 106)
        $outer.Dispose(); $inner.Dispose(); $pit.Dispose()
        return
    }

    if ($Name -match "eggplant") {
        $body = [System.Drawing.SolidBrush]::new((Convert-Color "#6F4BB2"))
        $Graphics.FillEllipse($body, 256, 224, 250, 282)
        $body.Dispose()
        Draw-Leaf $Graphics 322 182 124 76
        return
    }

    if ($Name -match "watermelon") {
        $green = [System.Drawing.SolidBrush]::new((Convert-Color "#3DA35D"))
        $red = [System.Drawing.SolidBrush]::new((Convert-Color "#FF6B7A"))
        $Graphics.FillEllipse($green, 238, 212, 292, 292)
        $Graphics.FillPie($red, 270, 244, 228, 228, 20, 140)
        $green.Dispose(); $red.Dispose()
        return
    }

    if ($Name -match "melon|cantaloupe|squash|pumpkin") {
        $body = [System.Drawing.SolidBrush]::new((Convert-Color "#F2B95B"))
        $Graphics.FillEllipse($body, 230, 226, 308, 274)
        $body.Dispose()
        Draw-Leaf $Graphics 340 184 106 74
        return
    }

    if ($Name -match "lime|lemon|grapefruit|orange") {
        $color = if ($Name -match "lime") { "#9AD65A" } elseif ($Name -match "lemon") { "#F7DB4F" } else { "#F28C6A" }
        $body = [System.Drawing.SolidBrush]::new((Convert-Color $color))
        $Graphics.FillEllipse($body, 250, 220, 268, 268)
        $body.Dispose()
        Draw-Leaf $Graphics 350 178 92 62
        return
    }

    $fruit = [System.Drawing.SolidBrush]::new($Accent)
    $Graphics.FillEllipse($fruit, 248, 220, 272, 272)
    $fruit.Dispose()
    Draw-Leaf $Graphics 348 176 94 66
}

function Draw-AnimalIcon {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Name,
        [System.Drawing.Color]$Accent
    )

    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(28, 47, 36, 48))
    $Graphics.FillEllipse($shadow, 210, 525, 348, 42)
    $shadow.Dispose()

    $body = [System.Drawing.SolidBrush]::new($Accent)
    $light = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(245, 255, 255, 255))
    $dark = [System.Drawing.SolidBrush]::new((Convert-Color "#2F2430"))

    if ($Name -match "butterfly|bee|ladybug") {
        $Graphics.FillEllipse($body, 210, 250, 155, 170)
        $Graphics.FillEllipse($body, 405, 250, 155, 170)
        $Graphics.FillEllipse($dark, 356, 270, 56, 166)
        $Graphics.FillEllipse($light, 256, 300, 38, 42)
        $Graphics.FillEllipse($light, 474, 300, 38, 42)
    } else {
        $Graphics.FillEllipse($body, 238, 288, 292, 190)
        $Graphics.FillEllipse($body, 292, 194, 184, 176)
        $Graphics.FillEllipse($body, 276, 184, 72, 72)
        $Graphics.FillEllipse($body, 420, 184, 72, 72)
        $Graphics.FillEllipse($light, 328, 284, 28, 30)
        $Graphics.FillEllipse($light, 412, 284, 28, 30)
        $Graphics.FillEllipse($dark, 338, 294, 10, 10)
        $Graphics.FillEllipse($dark, 422, 294, 10, 10)
        $Graphics.FillEllipse($light, 346, 332, 76, 48)
    }

    $body.Dispose(); $light.Dispose(); $dark.Dispose()
}

function Draw-ObjectIcon {
    param(
        [System.Drawing.Graphics]$Graphics,
        [string]$Name,
        [System.Drawing.Color]$Accent
    )

    $shadow = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(28, 47, 36, 48))
    $Graphics.FillEllipse($shadow, 210, 525, 348, 42)
    $shadow.Dispose()

    $body = [System.Drawing.SolidBrush]::new($Accent)
    $light = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(230, 255, 255, 255))
    $pen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(170, 255, 255, 255), 10)

    if ($Name -match "coin|bead|pearl|button|cap|stone|pinhead") {
        $Graphics.FillEllipse($body, 252, 224, 264, 264)
        $Graphics.DrawEllipse($pen, 286, 258, 196, 196)
    } elseif ($Name -match "umbrella") {
        $Graphics.FillPie($body, 220, 220, 328, 220, 180, 180)
        $Graphics.DrawLine($pen, 384, 332, 384, 506)
    } elseif ($Name -match "watermelon|pumpkin") {
        $Graphics.FillEllipse($body, 228, 226, 312, 266)
        $Graphics.DrawArc($pen, 280, 232, 88, 252, 80, 200)
        $Graphics.DrawArc($pen, 398, 232, 88, 252, -100, 200)
    } else {
        $path = New-RoundedPath 232 228 304 258 42
        $Graphics.FillPath($body, $path)
        $Graphics.DrawPath($pen, $path)
        $path.Dispose()
    }

    $Graphics.FillEllipse($light, 304, 278, 64, 42)
    $body.Dispose(); $light.Dispose(); $pen.Dispose()
}

function New-BabyDevelopmentImage {
    param(
        [int]$Week,
        [string]$ModeLabel,
        [string]$Shape,
        [string]$Name,
        [string]$AccentHex,
        [string]$LightHex,
        [string]$Path
    )

    $bitmap = [System.Drawing.Bitmap]::new(768, 768)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::ClearTypeGridFit

    $background = [System.Drawing.Drawing2D.LinearGradientBrush]::new(
        [System.Drawing.Rectangle]::new(0, 0, 768, 768),
        (Convert-Color $LightHex),
        (Convert-Color "#FFFFFF"),
        [System.Drawing.Drawing2D.LinearGradientMode]::ForwardDiagonal
    )
    $graphics.FillRectangle($background, 0, 0, 768, 768)
    $background.Dispose()

    $accent = Convert-Color $AccentHex
    $soft = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(56, $accent.R, $accent.G, $accent.B))
    $graphics.FillEllipse($soft, 72, 84, 152, 152)
    $graphics.FillEllipse($soft, 588, 132, 92, 92)
    $graphics.FillEllipse($soft, 92, 592, 106, 106)
    $soft.Dispose()

    $cardPath = New-RoundedPath 76 70 616 628 46
    $cardBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::FromArgb(238, 255, 255, 255))
    $cardPen = [System.Drawing.Pen]::new([System.Drawing.Color]::FromArgb(120, $accent.R, $accent.G, $accent.B), 3)
    $graphics.FillPath($cardBrush, $cardPath)
    $graphics.DrawPath($cardPen, $cardPath)
    $cardBrush.Dispose(); $cardPen.Dispose(); $cardPath.Dispose()

    $chipPath = New-RoundedPath 274 102 220 52 26
    $chipBrush = [System.Drawing.SolidBrush]::new($accent)
    $graphics.FillPath($chipBrush, $chipPath)
    $chipBrush.Dispose(); $chipPath.Dispose()

    $whiteBrush = [System.Drawing.SolidBrush]::new([System.Drawing.Color]::White)
    $darkBrush = [System.Drawing.SolidBrush]::new((Convert-Color "#2F2430"))
    $mutedBrush = [System.Drawing.SolidBrush]::new((Convert-Color "#847482"))
    $accentBrush = [System.Drawing.SolidBrush]::new($accent)

    $weekFont = [System.Drawing.Font]::new("Segoe UI", 22, [System.Drawing.FontStyle]::Bold)
    $modeFont = [System.Drawing.Font]::new("Segoe UI", 20, [System.Drawing.FontStyle]::Bold)
    $nameFontSize = if ($Name.Length -gt 18) { 34 } elseif ($Name.Length -gt 12) { 38 } else { 44 }
    $nameFont = [System.Drawing.Font]::new("Segoe UI", $nameFontSize, [System.Drawing.FontStyle]::Bold)

    Draw-CenteredText $graphics ("Week {0:D2}" -f $Week) ([System.Drawing.RectangleF]::new(0, 104, 768, 42)) $weekFont $whiteBrush

    if ($Shape -eq "fruit") {
        Draw-FruitIcon $graphics $Name $accent
    } elseif ($Shape -eq "animal") {
        Draw-AnimalIcon $graphics $Name $accent
    } else {
        Draw-ObjectIcon $graphics $Name $accent
    }

    Draw-CenteredText $graphics $ModeLabel ([System.Drawing.RectangleF]::new(0, 578, 768, 34)) $modeFont $accentBrush
    Draw-CenteredText $graphics $Name ([System.Drawing.RectangleF]::new(120, 622, 528, 72)) $nameFont $darkBrush

    $weekFont.Dispose(); $modeFont.Dispose(); $nameFont.Dispose()
    $whiteBrush.Dispose(); $darkBrush.Dispose(); $mutedBrush.Dispose(); $accentBrush.Dispose()
    $graphics.Dispose()

    $parent = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    $bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
    $bitmap.Dispose()
}

function Sql-Quote {
    param([string]$Value)
    return "'" + ($Value -replace "'", "''") + "'"
}

$resolvedOutputRoot = [System.IO.Path]::GetFullPath($OutputRoot)
$resolvedSeedPath = [System.IO.Path]::GetFullPath($SeedSqlPath)
New-Item -ItemType Directory -Force -Path $resolvedOutputRoot | Out-Null

foreach ($mode in $Modes) {
    $modeDir = Join-Path $resolvedOutputRoot $mode.Key
    New-Item -ItemType Directory -Force -Path $modeDir | Out-Null

    for ($index = 0; $index -lt 40; $index++) {
        $week = $index + 1
        $name = $mode.Items[$index]
        $fileName = "week_{0:D2}_{1}.png" -f $week, (Get-Slug $name)
        $path = Join-Path $modeDir $fileName

        New-BabyDevelopmentImage `
            -Week $week `
            -ModeLabel $mode.Label `
            -Shape $mode.Shape `
            -Name $name `
            -AccentHex $mode.Accent `
            -LightHex $mode.Light `
            -Path $path
    }
}

$seedRows = New-Object System.Collections.Generic.List[string]
for ($index = 0; $index -lt 40; $index++) {
    $week = $index + 1
    $name = $FruitComparisons[$index]
    $fileName = "week_{0:D2}_{1}.png" -f $week, (Get-Slug $name)
    $imageUrl = "/baby-development-images/fruit/$fileName"
    $seedRows.Add(("({0}, {1}, {2}, 'fruit', {1}, NOW())" -f $week, (Sql-Quote $name), (Sql-Quote $imageUrl)))
}

$seed = @"
-- Generated by scripts/baby-development-imagegen.ps1.
-- Safe to run repeatedly: one deterministic image mapping per pregnancy week.

ALTER TABLE IF EXISTS baby_developments
    ADD COLUMN IF NOT EXISTS image_url VARCHAR(600),
    ADD COLUMN IF NOT EXISTS comparison_type VARCHAR(40),
    ADD COLUMN IF NOT EXISTS comparison_name VARCHAR(120);

INSERT INTO baby_developments (
    week_number,
    baby_size,
    image_url,
    comparison_type,
    comparison_name,
    created_at
) VALUES
$($seedRows -join ",`r`n")
ON CONFLICT (week_number) DO UPDATE
SET
    baby_size = EXCLUDED.baby_size,
    image_url = EXCLUDED.image_url,
    comparison_type = EXCLUDED.comparison_type,
    comparison_name = EXCLUDED.comparison_name;
"@

New-Item -ItemType Directory -Force -Path (Split-Path -Parent $resolvedSeedPath) | Out-Null
Set-Content -LiteralPath $resolvedSeedPath -Value $seed -Encoding UTF8

Write-Output "Generated baby development images at $resolvedOutputRoot"
Write-Output "Generated seed SQL at $resolvedSeedPath"
