# Jarvis — Native Android App (True Background Listening)

Ye ek asli Android app hai (Kotlin). Foreground service ke through ye
phone lock hone par bhi "Jarvis" wake word sunta rehta hai — bilkul
webpage wale version se alag, ye real background service hai.

**Cost: ₹0.** Koi paid API nahi. Speech recognition Android ke apne
built-in `SpeechRecognizer` se hota hai (jaise "Ok Google" free hai,
waise hi ye free hai — koi API key ya billing nahi lagti).

---

## Kaise build karein (Termux se, bina Android Studio ke)

Phone par seedha Android SDK/Gradle chalana bahut heavy hai, isliye
build **GitHub Actions** (GitHub ki free cloud service) par hoga — tum
sirf code push karoge, APK apne aap ban jayega aur download link milega.

### Step 1 — Termux se GitHub par push karo

```bash
pkg update && pkg upgrade -y
pkg install git -y

cd ~
unzip jarvis-native.zip
cd jarvis-native

git init
git add .
git commit -m "Jarvis native assistant"
git branch -M main

# GitHub par pehle ek naya empty repo bana lo (naam: jarvis-native)
git remote add origin https://github.com/<tumhara-username>/jarvis-native.git
git push -u origin main
```

Login ke liye Personal Access Token chahiye hoga (GitHub → Settings →
Developer settings → Personal access tokens → naya token banao,
password ki jagah ye use karo). Ye bhi free hai.

### Step 2 — APK apne aap ban jayega

1. Push hote hi GitHub repo ke **Actions** tab me jao
2. "Build Jarvis APK" workflow chalte hue dikhega (3-5 min lagte hain)
3. Complete hone ke baad, us run ke andar **Artifacts** section me
   `jarvis-debug-apk` milega — download kar lo
4. Ye ek `.zip` hoga, andar `app-debug.apk` — usko phone me le jao aur
   install karo (Settings → allow install from unknown sources)

Agar workflow fail ho jaye to Actions tab me error log dikhega —
copy karke bata dena, fix kar denge.

---

## Setup karne ke baad

1. App open karo → **Settings** section me har contact ka number
   (+91 ke saath) aur Spotify album link daalo → **Save Settings**
2. **"Jarvis Start Karo"** button dabao — permissions maangega
   (Microphone, Call, Notifications) — sab allow karo
3. Ab app ko background me bhej sakte ho, screen lock bhi kar sakte ho
   — Jarvis ek chhota persistent notification ke saath sunta rahega

### Zaroori Android setting — Battery optimization band karo

Warna Android khud service ko kill kar dega:
**Settings → Apps → Jarvis → Battery → "Unrestricted" ya "No restrictions"**
select karo. Xiaomi/Oppo/Vivo phones me isके saath "Autostart" bhi
on karna padta hai (Settings → Apps → Autostart → Jarvis ON karo).

---

## Commands

| Bolo | Hoga |
|---|---|
| "Jarvis, Chaitanya ko call karo" | Seedha call lagega |
| "Jarvis, Gauri didi ko call karo" | Seedha call lagega |
| "Jarvis, Tanni ko call karo" | Seedha call lagega |
| "Jarvis, Mummy ko call karo" | Seedha call lagega |
| "Jarvis, Papa ko call karo" | Seedha call lagega |
| "Jarvis, Shankar ko call karo" | Seedha call lagega |
| "Jarvis, WhatsApp khol" | WhatsApp open |
| "Jarvis, Instagram khol" | Instagram open |
| "Jarvis, gana chalao" | Spotify album play |

Native app me call **seedha lagta hai** (dialer confirm karne ki
zaroorat nahi) kyunki CALL_PHONE permission diya hai.

---

## Sach me samajhne wali baatein

- Kuch phones (Xiaomi/Oppo/Vivo/OnePlus) apps ko aggressively background
  me kill karte hain battery bachane ke liye — upar wali battery
  optimization setting on karna zaroori hai warna Jarvis kuch der baad
  band ho jayega.
- `SpeechRecognizer` internet use karta hai processing ke liye (bilkul
  free, jaise Google Assistant), isliye data connection chahiye hoga.
- Ye app abhi debug build hai (testing ke liye) — signed release build
  chahiye ho to bata dena, wo bhi free me GitHub Actions se ban sakta
  hai.
