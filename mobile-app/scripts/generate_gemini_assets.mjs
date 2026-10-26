import fs from "node:fs";
import path from "node:path";

const MODELS = [
  "gemini-3.1-flash-image-preview",
  "gemini-2.5-flash-image",
];

const assets = {
  login_illustration:
    "Use case: ui-mockup asset. Asset type: Android login page illustration. Create a polished soft 3D illustration for a pregnancy health app login screen: a calm pregnant woman sitting comfortably with a phone, subtle heart and care symbols, warm cream background, soft pink and mint accents, modern professional healthcare style, rounded friendly shapes, clean negative space, no text, no logo, no watermark.",
  registration_illustration:
    "Use case: ui-mockup asset. Asset type: Android registration page illustration. Create a polished soft 3D illustration for a pregnancy health app registration screen: a welcoming mother-to-be creating a profile, gentle medical checklist and shield shapes, warm cream background, soft pink, peach and mint palette, modern professional healthcare style, rounded friendly shapes, no text, no logo, no watermark.",
  onboarding_image:
    "Use case: ui-mockup asset. Asset type: onboarding questionnaire illustration. Create a polished soft 3D illustration for pregnancy onboarding questions: a friendly health questionnaire, calendar, small check marks, and maternity care symbols, warm cream background, soft pink and lavender accents, clean mobile app composition, modern professional healthcare style, no readable text, no logo, no watermark.",
  pregnancy_tracking_illustration:
    "Use case: ui-mockup asset. Asset type: pregnancy tracking illustration. Create a polished soft 3D illustration showing pregnancy tracking: a week-by-week growth curve, calendar, small baby icon, and gentle progress rings, warm cream background, soft pink, coral and mint accents, modern professional healthcare style, rounded UI-friendly composition, no text, no logo, no watermark.",
  mother_baby_health_illustration:
    "Use case: ui-mockup asset. Asset type: baby and mother health illustration. Create a polished soft 3D illustration for mother and baby health: a mother holding her belly with a floating baby health heart, small fruit/object comparison icons, warm cream background, soft pink, mint and lavender palette, modern professional pregnancy app style, clean rounded shapes, no text, no logo, no watermark.",
};

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
  const requestedOutputDir = path.join(projectRoot, "src", "assets", "generated");
  const androidOutputDir = path.join(projectRoot, "app", "src", "main", "res", "drawable-nodpi");

  ensureDir(requestedOutputDir);
  ensureDir(androidOutputDir);

  const entries = Object.entries(assets);
  for (const [index, [name, prompt]] of entries.entries()) {
    const imageBytes = await generateImage(apiKey, prompt);
    const requestedPath = path.join(requestedOutputDir, `${name}.png`);
    const androidPath = path.join(androidOutputDir, `generated_${name}.png`);

    fs.writeFileSync(requestedPath, imageBytes);
    fs.copyFileSync(requestedPath, androidPath);
    console.log(`[${index + 1}/${entries.length}] saved ${requestedPath} and mirrored Android resource`);
    await new Promise((resolve) => setTimeout(resolve, 1000));
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
