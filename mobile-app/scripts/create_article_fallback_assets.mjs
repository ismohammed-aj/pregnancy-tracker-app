import fs from "node:fs";
import path from "node:path";

const articles = [
  ["article_nutrition_assiette", "Nutrition", "#FBEAF2", "#CFEFE7", "nutrition"],
  ["article_nutrition_micronutrients", "Nutrition", "#FFF8F1", "#EAD7F5", "nutrition"],
  ["article_nutrition_nausees", "Nutrition", "#FBEAF2", "#EEF9F7", "nutrition"],
  ["article_hydration", "Hydration", "#EEF9F7", "#CFEFE7", "hydration"],
  ["article_sante_signes", "Warning", "#FBEAF2", "#EAD7F5", "warning"],
  ["article_sante_sommeil", "Medical", "#EEF9F7", "#FBEAF2", "medical"],
  ["article_sante_poids", "Tracking", "#EAD7F5", "#CFEFE7", "tracking"],
  ["article_bebe_developpement", "Baby", "#FBEAF2", "#EAD7F5", "baby"],
  ["article_bebe_mouvements", "Baby", "#EEF9F7", "#FBEAF2", "baby"],
  ["article_activite_douce", "Activity", "#CFEFE7", "#FBEAF2", "activity"],
  ["article_activite_dos", "Activity", "#EEF9F7", "#EAD7F5", "activity"],
  ["article_mental_respiration", "Medical", "#EAD7F5", "#EEF9F7", "medical"],
  ["article_mental_soutien", "Medical", "#FBEAF2", "#EAD7F5", "medical"],
  ["article_rdv_consultation", "Appointments", "#FFF8F1", "#FBEAF2", "appointments"],
  ["article_rdv_echographie", "Appointments", "#EAD7F5", "#EEF9F7", "appointments"],
  ["article_conseils_valise", "Medical", "#FFF8F1", "#CFEFE7", "medical"],
  ["article_conseils_retour", "Medical", "#FBEAF2", "#EEF9F7", "medical"],
];

function ensureDir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function svgFor(key, label, start, end, icon) {
  return `<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="440" viewBox="0 0 720 440" role="img" aria-label="${label} maternity article thumbnail">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0" stop-color="${start}"/>
      <stop offset="1" stop-color="${end}"/>
    </linearGradient>
  </defs>
  <rect width="720" height="440" rx="48" fill="url(#bg)"/>
  <circle cx="620" cy="86" r="74" fill="#ffffff" opacity="0.45"/>
  <circle cx="112" cy="346" r="86" fill="#ffffff" opacity="0.35"/>
  <circle cx="360" cy="220" r="112" fill="#ffffff" opacity="0.72"/>
  <path d="${iconPath(icon)}" fill="none" stroke="#D6336C" stroke-width="22" stroke-linecap="round" stroke-linejoin="round"/>
  <path d="M246 312 C302 350 418 350 474 312" fill="none" stroke="#ffffff" stroke-width="18" stroke-linecap="round" opacity="0.78"/>
</svg>
`;
}

function vectorFor(key, start, end, icon) {
  return `<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="360dp"
    android:height="220dp"
    android:viewportWidth="720"
    android:viewportHeight="440">
    <path android:fillColor="${start}" android:pathData="M0,0h720v440h-720z" />
    <path android:fillColor="${end}" android:fillAlpha="0.72" android:pathData="M0,0h720v440h-720z" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.45" android:pathData="M620,12a74,74 0,1 0,0.1 0" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.35" android:pathData="M112,260a86,86 0,1 0,0.1 0" />
    <path android:fillColor="#FFFFFF" android:fillAlpha="0.72" android:pathData="M360,108a112,112 0,1 0,0.1 0" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="${iconPath(icon)}"
        android:strokeColor="#D6336C"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:strokeWidth="22" />
    <path
        android:fillColor="@android:color/transparent"
        android:pathData="M246,312C302,350 418,350 474,312"
        android:strokeColor="#FFFFFF"
        android:strokeAlpha="0.78"
        android:strokeLineCap="round"
        android:strokeWidth="18" />
</vector>
`;
}

function iconPath(icon) {
  switch (icon) {
    case "nutrition":
      return "M305,252C382,226 431,161 445,87C363,97 303,139 274,204C302,206 323,221 338,252 M287,278C324,221 368,180 425,136";
    case "hydration":
      return "M360,94C418,169 472,221 472,292C472,354 423,396 360,396C297,396 248,354 248,292C248,221 302,169 360,94 M322,308C322,337 339,354 365,354";
    case "warning":
      return "M360,102L512,364H208L360,102Z M360,194V274 M360,322V326";
    case "tracking":
      return "M220,336H502 M238,292L310,210L384,260L496,120 M310,210h1 M384,260h1 M496,120h1";
    case "baby":
      return "M360,160C414,160 458,202 458,256C458,316 412,360 360,360C308,360 262,316 262,256C262,202 306,160 360,160 M324,262H325 M396,262H397 M326,312C348,330 372,330 394,312 M402,140C390,112 364,104 340,122";
    case "activity":
      return "M376,126C397,126 414,143 414,164C414,185 397,202 376,202C355,202 338,185 338,164C338,143 355,126 376,126 M344,220L304,300L366,334L406,248L474,292 M330,334L270,398 M386,344L440,398";
    case "appointments":
      return "M248,144H472C494,144 512,162 512,184V344C512,366 494,384 472,384H248C226,384 208,366 208,344V184C208,162 226,144 248,144Z M208,218H512 M292,116V168 M428,116V168 M280,270H440 M280,320H394";
    default:
      return "M260,222C260,164 307,118 360,118C413,118 460,164 460,222C460,306 360,362 360,362C360,362 260,306 260,222Z M330,226H390 M360,196V256";
  }
}

function main() {
  const projectRoot = process.cwd();
  const svgDir = path.join(projectRoot, "src", "assets", "generated", "articles");
  const drawableDir = path.join(projectRoot, "app", "src", "main", "res", "drawable");
  ensureDir(svgDir);
  ensureDir(drawableDir);

  for (const [key, label, start, end, icon] of articles) {
    fs.writeFileSync(path.join(svgDir, `${key}.svg`), svgFor(key, label, start, end, icon));
    fs.writeFileSync(path.join(drawableDir, `fallback_${key}.xml`), vectorFor(key, start, end, icon));
    console.log(`created fallback ${key}`);
  }
}

main();
