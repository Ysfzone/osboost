> 🔗 **MANDATORY RULE CHAIN:** This visual system works strictly alongside [`PROJECT_CONTEXT.md`](PROJECT_CONTEXT.md) and [`RELEASE_AND_QUALITY_RULES.md`](RELEASE_AND_QUALITY_RULES.md). Every UI/web/README design must also include the mandatory `"By Ysfzone • X / GitHub: @Ysfzone"` attribution and dynamic version rules defined in `RELEASE_AND_QUALITY_RULES.md`.

# BRAND_GUIDELINES.md — OS B∞ST Visual Identity System

**Visual Source of Truth:** [`assets/brand_sheet.jpeg`](assets/brand_sheet.jpeg)  
**Master Vector Lockup:** [`assets/logo.svg`](assets/logo.svg)  
**Android Vector Emblem:** [`app/src/main/res/drawable/ic_osboost_rocket.xml`](app/src/main/res/drawable/ic_osboost_rocket.xml)

> **IMMUTABLE RULE:** No UI screen, popup, Android TV banner, launcher icon, GitHub README, web landing page, or promotional asset for **OS BOOST** (`com.osboost`) may alter the Infinity Rocket geometry, the `OS B∞ST` wordmark fusion, or introduce any color outside the 7 approved hex values below.

---

## 1. Exclusive 7-Color Palette (`res/values/colors.xml`)
Zero other colors (no green, orange, red, or unapproved shades) are permitted anywhere in the app, web assets, or documentation.

| Token | Hex | Role |
|---|---|---|
| `brand_bg_obsidian` | `#0D1117` | Deep Space Obsidian — Main dark background, monochrome dark base |
| `brand_bg_slate` | `#131922` | Elevated Slate — Popup card surface, Android TV banner background, GitHub dark containers |
| `brand_cyan_electric` | `#00E5FF` | Electric Cyan — Rocket top gradient stop, `"OS"` wordmark, active progress bars, neon borders, optimized status |
| `brand_blue_cobalt` | `#1E50B5` | Deep Cobalt Blue — Rocket bottom gradient stop, lower infinity loop, swept side fins |
| `brand_white_pure` | `#FFFFFF` | Pure White — `"B∞ST"` wordmark text, hero RAM numbers, dark-background monochrome lockup |
| `brand_black_pure` | `#000000` | Pure Black — Light-background monochrome lockup for print/documentation |
| `brand_silver_muted` | `#8A94A6` | Muted Metallic Silver — Secondary subtitles, neutral/analyzed status, metadata labels, 3D metallic highlights |

---

## 2. Exact Vector Geometry & Wordmark Rules

### Master Infinity Rocket Emblem
- **Fuselage:** Symmetrical vertical geometric rocket outline with crisp rounded stroke caps/joins and a sharp pointed apex.
- **Nose Cone:** Clean horizontal divider line separating the pointed upper nose cone.
- **Porthole:** Centered circular ring in the upper-middle fuselage.
- **Integrated Infinity Symbol (`∞`):** Smooth horizontal infinity loop fused seamlessly across the lower base of the rocket fuselage.
- **Fins & Exhaust:** Two symmetrical swept-back side fins flanking the infinity loop, with a sharp V-shaped exhaust chevron centered directly underneath.
- **Primary Gradient:** Linear vertical gradient from Electric Cyan (`#00E5FF`) at the nose cone to Deep Cobalt Blue (`#1E50B5`) across the infinity loop, fins, and exhaust chevron.

### `OS B∞ST` Wordmark
- Bold geometric uppercase sans-serif typography (`OS` + `B∞ST`).
- The two `'O'` letters in `"BOOST"` are fused into a horizontal infinity symbol (`∞`) matching the exact stroke weight of `'B'`, `'S'`, and `'T'`.
- In colored lockups: `"OS"` is `#00E5FF` and `"B∞ST"` is `#FFFFFF`.

---

## 3. The 6 Standard Brand Variants (`assets/brand_sheet.jpeg`)

1. **Variant 1 — Light Monochrome Black (`#000000` on `#FFFFFF`):**
   - **When to use:** Print documentation, invoices, light-mode PDFs, or single-ink stamping where color gradients are unavailable.
2. **Variant 2 — Dark Monochrome White (`#FFFFFF` on `#000000` / `#0D1117`):**
   - **When to use:** High-contrast dark overlays, monochrome status badges, or watermarked TV screen overlays.
3. **Variant 3 — Primary Colored Gradient (`#00E5FF` → `#1E50B5` on `#131922`):**
   - **When to use:** Primary digital hero lockup for the app popup (`activity_main.xml`), GitHub `README.md` header (`assets/logo.svg`), and web landing pages.
4. **Variant 4 — 3D Brushed Metallic Silver (`#8A94A6` on `#0D1117` / `#131922`):**
   - **When to use:** Hardware-style promotional renders, TV box packaging mockups, or metallic showcase headers.
5. **Variant 5 — Horizontal TV Banner Lockup (`res/drawable/tv_banner.xml`, 320dp × 180dp):**
   - **When to use:** Android TV / Google TV Leanback Home Screen channel banner.
   - **Borderless Dual-Corner Lighting & Divider-Free Rule:** Uses a **borderless** full-bleed `#131922` / `#0D1117` surface with soft dual-corner radial glow (`#1E50B5` / `#00E5FF` at top-left and bottom-right fading into `#0D1117`), **NO** perimeter stroke, and **NO** vertical divider line between the Infinity Rocket and the `OS B∞ST` wordmark.
   - **Multi-Density Raster Fallbacks:** `res/drawable-xhdpi/tv_banner.png` (`320×180 px`) and `res/drawable-xxhdpi/tv_banner.png` (`640×360 px`).
6. **Variant 6 — Frosted Dark App Icon Tile (`ic_launcher.xml` & `ic_launcher_round.xml`):**
   - **When to use:** Standard Android launcher icon and system settings app list.
   - **Borderless Full-Bleed Rule:** Uses a **borderless** `#131922` / `#0D1117` dual-corner glow adaptive background (`ic_launcher_background.xml`) with the centered colored Infinity Rocket Emblem (`18%` inset, strictly inside the `66dp/108dp` adaptive safe zone).
   - **Multi-Density Raster Fallbacks:** `mipmap-mdpi` (`48×48`), `mipmap-hdpi` (`72×72`), `mipmap-xhdpi` (`96×96`), `mipmap-xxhdpi` (`144×144`), `mipmap-xxxhdpi` (`192×192`), and [`assets/play_store_icon_512.png`](assets/play_store_icon_512.png) (`512×512`).

