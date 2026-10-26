import fs from "node:fs";
import path from "node:path";

const MODELS = [
  "gemini-3-pro-image-preview",
  "gemini-2.5-flash-image",
];

const assets = [
  {
    name: "baby_dev_seed",
    prompt: "Soft premium pregnancy tracking app illustration of an early pregnancy seed-size baby comparison, tiny seed in a clean protective circle, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.",
  },
  {
    name: "baby_dev_avocado",
    prompt: "Soft premium pregnancy tracking app illustration of an avocado baby-size comparison, centered avocado with gentle maternity health shapes, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.",
  },
  {
    name: "baby_dev_corn",
    prompt: "Soft premium pregnancy tracking app illustration of a celery or corn baby-size comparison for mid pregnancy, centered vegetable with gentle growth rings, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.",
  },
  {
    name: "baby_dev_melon",
    prompt: "Soft premium pregnancy tracking app illustration of a melon baby-size comparison, centered melon with gentle medical wellness shapes, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.",
  },
  {
    name: "baby_dev_pumpkin",
    prompt: "Soft premium pregnancy tracking app illustration of a small pumpkin baby-size comparison for late pregnancy, centered pumpkin with calm maternity wellness accents, pastel turquoise mint warm white palette, rounded clean mobile card style, no text, no logo, no watermark.",
  },
];

function findEnvFile() {
  const cwd = process.cwd();
  const candidates = [
    path.join(cwd, ".env"),
    path.join(cwd, "..", ".env"),
    path.join(cwd, "..", "backend-api", ".env"),
    path.join(cwd, "..", "..", "backend-api", ".env"),
  ];

  const found = candidates.find((candidate) => fs.existsSync(candidate));
  if (!found) {
    throw new Error("Could not find .env containing GEMINI_API_KEY");
  }

  return found;
}

function readGeminiKey(envFile) {
  const envValue = process.env.GEMINI_API_KEY?.trim();
  if (envValue) {
    return envValue;
  }

  const lines = fs.readFileSync(envFile, "utf8").split(/\r?\n/);

  for (const line of lines) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#") || !trimmed.includes("=")) continue;

    const index = trimmed.indexOf("=");
    const key = trimmed.slice(0, index).trim();
    const value = trimmed.slice(index + 1).trim().replace(/^['"]|['"]$/g, "");

    if (key === "GEMINI_API_KEY" && value) {
      return value;
    }
  }

  throw new Error("GEMINI_API_KEY not found in .env");
}

async function requestImage(apiKey, model, prompt) {
  const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "x-goog-api-key": apiKey,
    },
    body: JSON.stringify({
      contents: [
        {
          parts: [{ text: prompt }],
        },
      ],
    }),
  });

  const responseBody = await response.text();
  if (!response.ok) {
    const error = new Error(`Gemini API request failed for ${model}: HTTP ${response.status}: ${responseBody}`);
    error.status = response.status;
    throw error;
  }

  const data = JSON.parse(responseBody);
  const candidates = data.candidates ?? [];

  for (const candidate of candidates) {
    const parts = candidate.content?.parts ?? [];
    for (const part of parts) {
      const inlineData = part.inlineData ?? part.inline_data;
      if (inlineData?.data) {
        return Buffer.from(inlineData.data, "base64");
      }
    }
  }

  throw new Error(`Gemini response did not include image data for ${model}`);
}

async function generateImage(apiKey, prompt) {
  let lastError;

  for (const model of MODELS) {
    try {
      return await requestImage(apiKey, model, prompt);
    } catch (error) {
      lastError = error;
      const canTryNextModel = error.status === 429 || error.status === 404 || error.status === 400;
      if (!canTryNextModel) break;
      console.warn(`Model ${model} failed; trying next configured Nano Banana model.`);
    }
  }

  throw lastError;
}

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

async function main() {
  const apiKey = readGeminiKey(findEnvFile());
  const projectRoot = process.cwd();
  const requestedOutputDir = path.join(projectRoot, "src", "assets", "generated", "baby-development");
  const androidOutputDir = path.join(projectRoot, "app", "src", "main", "res", "drawable-nodpi");

  ensureDir(requestedOutputDir);
  ensureDir(androidOutputDir);

  for (const [index, asset] of assets.entries()) {
    const imageBytes = await generateImage(apiKey, asset.prompt);
    const requestedPath = path.join(requestedOutputDir, `${asset.name}.png`);
    const androidPath = path.join(androidOutputDir, `${asset.name}.png`);

    fs.writeFileSync(requestedPath, imageBytes);
    fs.copyFileSync(requestedPath, androidPath);
    console.log(`[${index + 1}/${assets.length}] saved ${asset.name}`);
    await new Promise((resolve) => setTimeout(resolve, 1200));
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
