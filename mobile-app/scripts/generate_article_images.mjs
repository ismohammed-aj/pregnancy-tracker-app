import fs from "node:fs";
import path from "node:path";

const MODELS = [
  "gemini-3.1-flash-image-preview",
  "gemini-2.5-flash-image",
];

const articles = [
  {
    key: "article_nutrition_assiette",
    title: "Construire une assiette équilibrée pendant la grossesse",
    category: "Nutrition",
    icon: "nutrition",
    prompt: "Soft pastel flat illustration of a pregnant woman eating a healthy balanced meal at a small table, maternity wellness app article thumbnail, modern mobile app style, pink lavender mint palette, rounded shapes, clean background, no text, no logo, no watermark.",
  },
  {
    key: "article_nutrition_micronutrients",
    title: "Fer, folates et calcium sans se compliquer",
    category: "Nutrition",
    icon: "nutrition",
    prompt: "Soft pastel flat illustration of prenatal nutrition essentials with leafy greens, lentils, dairy, and a gentle maternity health symbol, modern mobile app article thumbnail, pink lavender mint palette, no text, no logo, no scary medical imagery.",
  },
  {
    key: "article_nutrition_nausees",
    title: "Nausées et petits repas adaptés",
    category: "Nutrition",
    icon: "nutrition",
    prompt: "Soft pastel flat illustration of a pregnant woman calmly choosing small simple snacks and a glass of water, clean maternity wellness app article thumbnail, rounded shapes, pink lavender mint palette, no text, no logo, no watermark.",
  },
  {
    key: "article_sante_signes",
    title: "Les signes à surveiller sans paniquer",
    category: "Santé",
    icon: "warning",
    prompt: "Soft pastel flat illustration of a pregnant woman checking a simple health checklist with a caring clinician icon nearby, reassuring maternity health app article thumbnail, clean background, pink lavender mint palette, no text, no emergency imagery, no logo.",
  },
  {
    key: "article_sante_sommeil",
    title: "Sommeil, fatigue et récupération",
    category: "Santé",
    icon: "medical_advice",
    prompt: "Soft pastel flat illustration of a pregnant woman resting comfortably with pillows, moon, stars, and gentle wellness symbols, calm maternity health app article thumbnail, pink lavender mint palette, rounded shapes, no text, no logo.",
  },
  {
    key: "article_sante_poids",
    title: "Comprendre le suivi du poids",
    category: "Santé",
    icon: "pregnancy_tracking",
    prompt: "Soft pastel flat illustration of pregnancy weight tracking with a gentle growth chart, scale icon, and a pregnant woman standing calmly, modern maternity app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_bebe_developpement",
    title: "Bébé grandit semaine après semaine",
    category: "Bébé",
    icon: "baby_development",
    prompt: "Soft pastel flat illustration of week-by-week baby development with fruit comparison shapes, calendar, and a gentle baby icon, pregnancy tracking app article thumbnail, pink lavender mint palette, clean background, no text, no logo.",
  },
  {
    key: "article_bebe_mouvements",
    title: "Mouvements du bébé et moments calmes",
    category: "Bébé",
    icon: "baby_development",
    prompt: "Soft pastel flat illustration of a pregnant woman sitting quietly with hands on belly and soft movement waves, calm maternity app article thumbnail, rounded shapes, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_activite_douce",
    title: "Bouger en douceur pendant la grossesse",
    category: "Activité",
    icon: "physical_activity",
    prompt: "Soft pastel flat illustration of a pregnant woman walking gently in a peaceful park, maternity wellness app article thumbnail, mint lavender pink palette, clean rounded shapes, no text, no logo, no watermark.",
  },
  {
    key: "article_activite_dos",
    title: "Soulager le dos avec des gestes simples",
    category: "Activité",
    icon: "physical_activity",
    prompt: "Soft pastel flat illustration of a pregnant woman doing safe gentle back stretches with pillows and calm wellness shapes, pregnancy health app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_mental_respiration",
    title: "Stress, émotions et respiration",
    category: "Mental",
    icon: "medical_advice",
    prompt: "Soft pastel flat illustration of a pregnant woman practicing calm breathing with floating soft circles and heart symbols, mental wellness maternity app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_mental_soutien",
    title: "Préparer son cercle de soutien",
    category: "Mental",
    icon: "medical_advice",
    prompt: "Soft pastel flat illustration of a pregnant woman supported by caring family and health professional silhouettes, warm maternity community app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_rdv_consultation",
    title: "Bien préparer une consultation prénatale",
    category: "Rendez-vous",
    icon: "appointments",
    prompt: "Soft pastel flat illustration of a prenatal appointment checklist, calendar, and pregnant woman talking with a caring clinician, professional maternity app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_rdv_echographie",
    title: "Échographies et questions utiles",
    category: "Rendez-vous",
    icon: "appointments",
    prompt: "Soft pastel flat illustration of a prenatal ultrasound appointment represented gently with a screen and caring clinician, no realistic medical details, maternity app article thumbnail, pink lavender mint palette, no text, no logo.",
  },
  {
    key: "article_conseils_valise",
    title: "Préparer la valise de maternité sereinement",
    category: "Conseils",
    icon: "medical_advice",
    prompt: "Soft pastel flat illustration of a neatly prepared maternity hospital bag with baby clothes and documents, pregnancy app article thumbnail, pink lavender mint palette, rounded shapes, no text, no logo.",
  },
  {
    key: "article_conseils_retour",
    title: "Retour à la maison les premiers jours",
    category: "Conseils",
    icon: "medical_advice",
    prompt: "Soft pastel flat illustration of a mother and newborn arriving home in a calm cozy room, maternity care app article thumbnail, pink lavender mint palette, rounded shapes, no text, no logo.",
  },
  {
    key: "article_hydration",
    title: "Hydratation pendant la grossesse",
    category: "Nutrition",
    icon: "hydration",
    prompt: "Soft pastel flat illustration of a pregnant woman drinking water with fruit infused water and gentle wellness shapes, maternity health app article thumbnail, pink lavender mint palette, clean background, no text, no logo.",
  },
];

function findEnvFile() {
  const cwd = process.cwd();
  const candidates = [
    path.join(cwd, ".env"),
    path.join(cwd, "..", ".env"),
    path.join(cwd, "backend-api", ".env"),
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

function buildPrompt(article) {
  return [
    "Use case: ui-mockup asset.",
    "Asset type: responsive Android article thumbnail.",
    `Primary request: ${article.prompt}`,
    "Style/medium: modern soft flat illustration for a pregnancy and maternity health mobile app.",
    "Composition/framing: centered subject, generous padding, works when cropped to rounded card thumbnail.",
    "Color palette: pastel pink, lavender, mint, warm white.",
    "Constraints: no text inside the image, no logos, no watermark, no scary or realistic medical imagery, no copyrighted characters.",
  ].join(" ");
}

async function main() {
  const projectRoot = process.cwd();
  const apiKey = readGeminiKey(findEnvFile());
  const requestedOutputDir = path.join(projectRoot, "src", "assets", "generated", "articles");
  const androidOutputDir = path.join(projectRoot, "app", "src", "main", "res", "drawable-nodpi");

  ensureDir(requestedOutputDir);
  ensureDir(androidOutputDir);

  for (const [index, article] of articles.entries()) {
    const prompt = buildPrompt(article);
    const imageBytes = await generateImage(apiKey, prompt);
    const requestedPath = path.join(requestedOutputDir, `${article.key}.png`);
    const androidPath = path.join(androidOutputDir, `${article.key}.png`);

    fs.writeFileSync(requestedPath, imageBytes);
    fs.copyFileSync(requestedPath, androidPath);
    console.log(`[${index + 1}/${articles.length}] ${article.key}`);
    await new Promise((resolve) => setTimeout(resolve, 1200));
  }
}

main().catch((error) => {
  console.error(error.message);
  process.exit(1);
});
