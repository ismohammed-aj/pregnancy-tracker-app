import fs from "node:fs";
import path from "node:path";
import zlib from "node:zlib";

const projectRoot = process.cwd();
const svgDir = path.join(projectRoot, "src", "assets", "generated", "baby-development");
const drawableDir = path.join(projectRoot, "app", "src", "main", "res", "drawable");
const drawablePngDir = path.join(projectRoot, "app", "src", "main", "res", "drawable-nodpi");

const assets = [
  {
    name: "baby_dev_seed",
    label: "early seed",
    bg: "#EAFBF8",
    accent: "#23B8AE",
    soft: "#CFF3ED",
    shape: `<circle cx="360" cy="355" r="86" fill="#23B8AE"/>
  <path d="M316 350 C320 302, 356 268, 398 286 C438 304, 440 362, 398 398 C356 434, 312 400, 316 350Z" fill="#ffffff" opacity="0.92"/>
  <path d="M358 316 C382 314, 402 330, 408 352" fill="none" stroke="#23B8AE" stroke-width="18" stroke-linecap="round"/>`,
    vector: [
      `<path android:fillColor="#23B8AE" android:pathData="M360,269a86,86 0,1 0,0.1 0" />`,
      `<path android:fillColor="#FFFFFF" android:fillAlpha="0.92" android:pathData="M316,350C320,302 356,268 398,286C438,304 440,362 398,398C356,434 312,400 316,350Z" />`,
      `<path android:fillColor="@android:color/transparent" android:pathData="M358,316C382,314 402,330 408,352" android:strokeColor="#23B8AE" android:strokeWidth="18" android:strokeLineCap="round" />`,
    ],
  },
  {
    name: "baby_dev_avocado",
    label: "avocado",
    bg: "#F0FBF7",
    accent: "#42B883",
    soft: "#D6F4DD",
    shape: `<path d="M360 160 C470 192, 520 300, 478 426 C440 540, 286 548, 238 432 C190 316, 248 188, 360 160Z" fill="#42B883"/>
  <path d="M360 204 C430 226, 464 304, 434 402 C408 486, 304 492, 276 406 C244 308, 292 226, 360 204Z" fill="#EAF8DB"/>
  <circle cx="360" cy="382" r="52" fill="#C98F50"/>`,
    vector: [
      `<path android:fillColor="#42B883" android:pathData="M360,160C470,192 520,300 478,426C440,540 286,548 238,432C190,316 248,188 360,160Z" />`,
      `<path android:fillColor="#EAF8DB" android:pathData="M360,204C430,226 464,304 434,402C408,486 304,492 276,406C244,308 292,226 360,204Z" />`,
      `<path android:fillColor="#C98F50" android:pathData="M360,330a52,52 0,1 0,0.1 0" />`,
    ],
  },
  {
    name: "baby_dev_corn",
    label: "corn",
    bg: "#FFF8E7",
    accent: "#F2B84B",
    soft: "#DFF5E8",
    shape: `<path d="M354 124 C448 174, 482 306, 428 456 C396 546, 310 548, 274 458 C218 316, 258 178, 354 124Z" fill="#F2B84B"/>
  <path d="M270 434 C220 350, 232 260, 286 202 C278 312, 300 408, 360 520 C326 506, 294 474, 270 434Z" fill="#65C9A8"/>
  <path d="M448 434 C500 350, 488 260, 434 202 C442 312, 420 408, 360 520 C394 506, 424 474, 448 434Z" fill="#65C9A8"/>
  <path d="M326 214 L408 478 M390 214 L310 478 M282 300 H440 M284 380 H436" stroke="#FFF3B0" stroke-width="18" stroke-linecap="round"/>`,
    vector: [
      `<path android:fillColor="#F2B84B" android:pathData="M354,124C448,174 482,306 428,456C396,546 310,548 274,458C218,316 258,178 354,124Z" />`,
      `<path android:fillColor="#65C9A8" android:pathData="M270,434C220,350 232,260 286,202C278,312 300,408 360,520C326,506 294,474 270,434Z" />`,
      `<path android:fillColor="#65C9A8" android:pathData="M448,434C500,350 488,260 434,202C442,312 420,408 360,520C394,506 424,474 448,434Z" />`,
      `<path android:fillColor="@android:color/transparent" android:pathData="M326,214L408,478M390,214L310,478M282,300H440M284,380H436" android:strokeColor="#FFF3B0" android:strokeWidth="18" android:strokeLineCap="round" />`,
    ],
  },
  {
    name: "baby_dev_melon",
    label: "melon",
    bg: "#F4FBF9",
    accent: "#54C8B6",
    soft: "#E7D7F5",
    shape: `<ellipse cx="360" cy="360" rx="166" ry="132" fill="#54C8B6"/>
  <ellipse cx="360" cy="360" rx="116" ry="98" fill="#E5FFF5" opacity="0.9"/>
  <path d="M250 360 H470 M300 270 C330 338, 330 382, 300 450 M420 270 C390 338, 390 382, 420 450" stroke="#ffffff" stroke-width="16" stroke-linecap="round" opacity="0.8"/>`,
    vector: [
      `<path android:fillColor="#54C8B6" android:pathData="M360,228a166,132 0,1 0,0.1 0" />`,
      `<path android:fillColor="#E5FFF5" android:fillAlpha="0.9" android:pathData="M360,262a116,98 0,1 0,0.1 0" />`,
      `<path android:fillColor="@android:color/transparent" android:pathData="M250,360H470M300,270C330,338 330,382 300,450M420,270C390,338 390,382 420,450" android:strokeColor="#FFFFFF" android:strokeAlpha="0.8" android:strokeWidth="16" android:strokeLineCap="round" />`,
    ],
  },
  {
    name: "baby_dev_pumpkin",
    label: "pumpkin",
    bg: "#FFF6EA",
    accent: "#F2A34A",
    soft: "#D7F3EC",
    shape: `<path d="M360 198 C484 198, 560 282, 536 396 C514 502, 430 556, 360 536 C290 556, 206 502, 184 396 C160 282, 236 198, 360 198Z" fill="#F2A34A"/>
  <path d="M360 212 C410 272, 414 458, 360 536 C306 458, 310 272, 360 212Z" fill="#F8BC63"/>
  <path d="M360 198 C358 162, 378 144, 412 136" fill="none" stroke="#57A773" stroke-width="22" stroke-linecap="round"/>`,
    vector: [
      `<path android:fillColor="#F2A34A" android:pathData="M360,198C484,198 560,282 536,396C514,502 430,556 360,536C290,556 206,502 184,396C160,282 236,198 360,198Z" />`,
      `<path android:fillColor="#F8BC63" android:pathData="M360,212C410,272 414,458 360,536C306,458 310,272 360,212Z" />`,
      `<path android:fillColor="@android:color/transparent" android:pathData="M360,198C358,162 378,144 412,136" android:strokeColor="#57A773" android:strokeWidth="22" android:strokeLineCap="round" />`,
    ],
  },
];

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function rgba(hex, alpha = 1) {
  const normalized = hex.replace("#", "");
  return {
    r: Number.parseInt(normalized.slice(0, 2), 16),
    g: Number.parseInt(normalized.slice(2, 4), 16),
    b: Number.parseInt(normalized.slice(4, 6), 16),
    a: Math.round(alpha * 255),
  };
}

function createCanvas(width, height, fill) {
  const pixels = Buffer.alloc(width * height * 4);
  const color = rgba(fill);

  for (let index = 0; index < pixels.length; index += 4) {
    pixels[index] = color.r;
    pixels[index + 1] = color.g;
    pixels[index + 2] = color.b;
    pixels[index + 3] = 255;
  }

  return { width, height, pixels };
}

function blendPixel(canvas, x, y, color, coverage = 1) {
  if (x < 0 || y < 0 || x >= canvas.width || y >= canvas.height || coverage <= 0) return;

  const alpha = (color.a / 255) * Math.min(1, coverage);
  const index = (y * canvas.width + x) * 4;

  canvas.pixels[index] = Math.round(color.r * alpha + canvas.pixels[index] * (1 - alpha));
  canvas.pixels[index + 1] = Math.round(color.g * alpha + canvas.pixels[index + 1] * (1 - alpha));
  canvas.pixels[index + 2] = Math.round(color.b * alpha + canvas.pixels[index + 2] * (1 - alpha));
  canvas.pixels[index + 3] = 255;
}

function fillEllipse(canvas, cx, cy, rx, ry, color) {
  const edge = 1.8 / Math.min(rx, ry);
  const minX = Math.max(0, Math.floor(cx - rx - 2));
  const maxX = Math.min(canvas.width - 1, Math.ceil(cx + rx + 2));
  const minY = Math.max(0, Math.floor(cy - ry - 2));
  const maxY = Math.min(canvas.height - 1, Math.ceil(cy + ry + 2));

  for (let y = minY; y <= maxY; y += 1) {
    for (let x = minX; x <= maxX; x += 1) {
      const dx = (x + 0.5 - cx) / rx;
      const dy = (y + 0.5 - cy) / ry;
      const distance = dx * dx + dy * dy;
      if (distance <= 1 + edge) {
        blendPixel(canvas, x, y, color, distance <= 1 ? 1 : 1 - (distance - 1) / edge);
      }
    }
  }
}

function fillCircle(canvas, cx, cy, radius, color) {
  fillEllipse(canvas, cx, cy, radius, radius, color);
}

function distanceToSegment(px, py, x1, y1, x2, y2) {
  const vx = x2 - x1;
  const vy = y2 - y1;
  const wx = px - x1;
  const wy = py - y1;
  const lengthSquared = vx * vx + vy * vy;
  const t = lengthSquared === 0 ? 0 : Math.max(0, Math.min(1, (wx * vx + wy * vy) / lengthSquared));
  const x = x1 + t * vx;
  const y = y1 + t * vy;
  return Math.hypot(px - x, py - y);
}

function strokeLine(canvas, x1, y1, x2, y2, radius, color) {
  const minX = Math.max(0, Math.floor(Math.min(x1, x2) - radius - 2));
  const maxX = Math.min(canvas.width - 1, Math.ceil(Math.max(x1, x2) + radius + 2));
  const minY = Math.max(0, Math.floor(Math.min(y1, y2) - radius - 2));
  const maxY = Math.min(canvas.height - 1, Math.ceil(Math.max(y1, y2) + radius + 2));

  for (let y = minY; y <= maxY; y += 1) {
    for (let x = minX; x <= maxX; x += 1) {
      const distance = distanceToSegment(x + 0.5, y + 0.5, x1, y1, x2, y2);
      if (distance <= radius + 1.5) {
        blendPixel(canvas, x, y, color, distance <= radius ? 1 : 1 - (distance - radius) / 1.5);
      }
    }
  }
}

function strokePolyline(canvas, points, radius, color) {
  for (let index = 0; index < points.length - 1; index += 1) {
    strokeLine(canvas, points[index][0], points[index][1], points[index + 1][0], points[index + 1][1], radius, color);
  }
}

function pointInPolygon(x, y, points) {
  let inside = false;

  for (let index = 0, previous = points.length - 1; index < points.length; previous = index, index += 1) {
    const xi = points[index][0];
    const yi = points[index][1];
    const xj = points[previous][0];
    const yj = points[previous][1];
    const intersects = yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi;

    if (intersects) inside = !inside;
  }

  return inside;
}

function fillPolygon(canvas, points, color) {
  const xs = points.map(([x]) => x);
  const ys = points.map(([, y]) => y);
  const minX = Math.max(0, Math.floor(Math.min(...xs)));
  const maxX = Math.min(canvas.width - 1, Math.ceil(Math.max(...xs)));
  const minY = Math.max(0, Math.floor(Math.min(...ys)));
  const maxY = Math.min(canvas.height - 1, Math.ceil(Math.max(...ys)));

  for (let y = minY; y <= maxY; y += 1) {
    for (let x = minX; x <= maxX; x += 1) {
      if (pointInPolygon(x + 0.5, y + 0.5, points)) {
        blendPixel(canvas, x, y, color);
      }
    }
  }
}

function renderPng(asset) {
  const canvas = createCanvas(720, 720, asset.bg);

  fillCircle(canvas, 116, 124, 72, rgba("#FFFFFF", 0.58));
  fillCircle(canvas, 604, 164, 92, rgba(asset.soft, 0.45));
  fillCircle(canvas, 592, 588, 98, rgba("#FFFFFF", 0.48));
  fillCircle(canvas, 360, 360, 206, rgba("#FFFFFF", 0.54));
  fillEllipse(canvas, 360, 562, 154, 26, rgba("#4A7A78", 0.08));

  if (asset.name === "baby_dev_seed") {
    fillCircle(canvas, 360, 355, 86, rgba(asset.accent));
    fillEllipse(canvas, 360, 356, 50, 72, rgba("#FFFFFF", 0.92));
    strokePolyline(canvas, [[358, 316], [382, 314], [402, 330], [408, 352]], 9, rgba(asset.accent));
  }

  if (asset.name === "baby_dev_avocado") {
    fillEllipse(canvas, 360, 350, 132, 190, rgba(asset.accent));
    fillEllipse(canvas, 360, 352, 88, 144, rgba("#EAF8DB"));
    fillCircle(canvas, 360, 382, 52, rgba("#C98F50"));
    fillEllipse(canvas, 320, 258, 24, 44, rgba("#FFFFFF", 0.24));
  }

  if (asset.name === "baby_dev_corn") {
    fillEllipse(canvas, 354, 330, 92, 210, rgba(asset.accent));
    fillPolygon(canvas, [[270, 434], [220, 350], [232, 260], [286, 202], [300, 408], [360, 520], [326, 506]], rgba("#65C9A8"));
    fillPolygon(canvas, [[448, 434], [500, 350], [488, 260], [434, 202], [420, 408], [360, 520], [394, 506]], rgba("#65C9A8"));
    strokeLine(canvas, 326, 214, 408, 478, 9, rgba("#FFF3B0", 0.9));
    strokeLine(canvas, 390, 214, 310, 478, 9, rgba("#FFF3B0", 0.9));
    strokeLine(canvas, 282, 300, 440, 300, 9, rgba("#FFF3B0", 0.9));
    strokeLine(canvas, 284, 380, 436, 380, 9, rgba("#FFF3B0", 0.9));
  }

  if (asset.name === "baby_dev_melon") {
    fillEllipse(canvas, 360, 360, 166, 132, rgba(asset.accent));
    fillEllipse(canvas, 360, 360, 116, 98, rgba("#E5FFF5", 0.9));
    strokeLine(canvas, 250, 360, 470, 360, 8, rgba("#FFFFFF", 0.8));
    strokePolyline(canvas, [[300, 270], [328, 338], [328, 382], [300, 450]], 8, rgba("#FFFFFF", 0.8));
    strokePolyline(canvas, [[420, 270], [392, 338], [392, 382], [420, 450]], 8, rgba("#FFFFFF", 0.8));
  }

  if (asset.name === "baby_dev_pumpkin") {
    fillEllipse(canvas, 278, 382, 96, 154, rgba(asset.accent));
    fillEllipse(canvas, 442, 382, 96, 154, rgba(asset.accent));
    fillEllipse(canvas, 360, 382, 134, 168, rgba(asset.accent));
    fillEllipse(canvas, 360, 384, 54, 156, rgba("#F8BC63"));
    strokePolyline(canvas, [[360, 198], [358, 162], [378, 144], [412, 136]], 11, rgba("#57A773"));
  }

  return canvas;
}

let crcTable;

function getCrcTable() {
  if (crcTable) return crcTable;

  crcTable = new Uint32Array(256);
  for (let index = 0; index < 256; index += 1) {
    let crc = index;
    for (let bit = 0; bit < 8; bit += 1) {
      crc = crc & 1 ? 0xedb88320 ^ (crc >>> 1) : crc >>> 1;
    }
    crcTable[index] = crc >>> 0;
  }

  return crcTable;
}

function crc32(buffer) {
  const table = getCrcTable();
  let crc = 0xffffffff;

  for (const byte of buffer) {
    crc = table[(crc ^ byte) & 0xff] ^ (crc >>> 8);
  }

  return (crc ^ 0xffffffff) >>> 0;
}

function pngChunk(type, data) {
  const length = Buffer.alloc(4);
  const name = Buffer.from(type);
  const checksum = Buffer.alloc(4);

  length.writeUInt32BE(data.length, 0);
  checksum.writeUInt32BE(crc32(Buffer.concat([name, data])), 0);

  return Buffer.concat([length, name, data, checksum]);
}

function writePng(filePath, canvas) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(canvas.width, 0);
  ihdr.writeUInt32BE(canvas.height, 4);
  ihdr[8] = 8;
  ihdr[9] = 6;
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;

  const stride = canvas.width * 4;
  const raw = Buffer.alloc((stride + 1) * canvas.height);

  for (let y = 0; y < canvas.height; y += 1) {
    raw[y * (stride + 1)] = 0;
    canvas.pixels.copy(raw, y * (stride + 1) + 1, y * stride, (y + 1) * stride);
  }

  fs.writeFileSync(
    filePath,
    Buffer.concat([
      Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]),
      pngChunk("IHDR", ihdr),
      pngChunk("IDAT", zlib.deflateSync(raw, { level: 9 })),
      pngChunk("IEND", Buffer.alloc(0)),
    ]),
  );
}

function svgFor(asset) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="720" viewBox="0 0 720 720" role="img" aria-label="${asset.label} baby development illustration">
  <rect width="720" height="720" rx="72" fill="${asset.bg}"/>
  <circle cx="116" cy="124" r="72" fill="#FFFFFF" opacity="0.58"/>
  <circle cx="604" cy="164" r="92" fill="${asset.soft}" opacity="0.45"/>
  <circle cx="592" cy="588" r="98" fill="#FFFFFF" opacity="0.48"/>
  <circle cx="360" cy="360" r="206" fill="#FFFFFF" opacity="0.54"/>
  ${asset.shape}
</svg>
`;
}

function vectorFor(asset) {
  return `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="240dp"
    android:height="240dp"
    android:viewportWidth="720"
    android:viewportHeight="720">
    <path android:fillColor="${asset.bg}" android:pathData="M0,0h720v720h-720z" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.58" android:pathData="M116,52a72,72 0,1 0,0.1 0" />
    <path android:fillColor="${asset.soft}" android:fillAlpha="0.45" android:pathData="M604,72a92,92 0,1 0,0.1 0" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.48" android:pathData="M592,490a98,98 0,1 0,0.1 0" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.54" android:pathData="M360,154a206,206 0,1 0,0.1 0" />
    ${asset.vector.join("\n    ")}
</vector>
`;
}

const prompts = assets.map((asset) => {
  return `${asset.name}: Soft premium pregnancy tracking app illustration of ${asset.label}, centered fruit/vegetable or baby-size comparison, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.`;
});

ensureDir(svgDir);
ensureDir(drawableDir);
ensureDir(drawablePngDir);

for (const asset of assets) {
  const pngPath = path.join(svgDir, `${asset.name}.png`);

  fs.writeFileSync(path.join(svgDir, `${asset.name}.svg`), svgFor(asset));
  fs.writeFileSync(path.join(drawableDir, `${asset.name}.xml`), vectorFor(asset));
  writePng(pngPath, renderPng(asset));
  fs.copyFileSync(pngPath, path.join(drawablePngDir, `${asset.name}.png`));
}

fs.writeFileSync(path.join(svgDir, "baby_development_prompts.txt"), `${prompts.join("\n")}\n`);
console.log(`Created ${assets.length} fallback baby development SVG/vector/PNG assets.`);
