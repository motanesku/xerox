# Phaser 3020 Helper

Mic proiect Android Studio pentru Xerox Phaser 3020.

## Ce face
- salvează IP-ul sau hostname-ul imprimantei;
- deschide rapid pagina web a imprimantei;
- deschide setările Wi-Fi;
- alege un PDF din telefon și lansează dialogul nativ Android de print.

## Ce NU poate face
- nu poate forța o conexiune Wi-Fi Direct permanentă în fundal;
- nu poate ocoli limitările Android privind procesele și scanările din fundal.

## Cum îl deschizi
1. Deschide proiectul în Android Studio.
2. Lasă Android Studio să descarce Gradle.
3. Rulează pe telefon sau emulator Android.

## Flux recomandat
1. Conectează imprimanta la routerul Wi-Fi din casă.
2. Află IP-ul imprimantei.
3. Introdu IP-ul în aplicație și salvează-l.
4. Alege PDF și printează.

## Observație
Pentru print efectiv, Android folosește serviciul de print instalat pe telefon (de multe ori Mopria / serviciul implicit de print).


## Build APK on GitHub
1. Upload this project to a GitHub repo.
2. Open the **Actions** tab.
3. Run **Build Android APK**.
4. Download the artifact **Phaser3020Helper-debug-apk**.
