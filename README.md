# 📊 YouTube Analytics & Insights (Android)

Application Android native développée en **Kotlin** et **Jetpack Compose** (Architecture MVP / Clean Architecture avec Room Database et Retrofit).

---

## 🚀 Compilation automatique en 1 clic via GitHub Actions (Sans Android Studio)

Vous n'avez pas besoin d'installer Android Studio sur votre machine pour compiler et obtenir l'application !

### Étapes rapides :

1. **Poussez ce projet sur votre compte GitHub** (via *Push to GitHub* ou en créant un nouveau dépôt).
2. Rendez-vous sur votre dépôt GitHub et cliquez sur l'onglet **Actions** en haut.
3. Cliquez sur le workflow **`Android CI - Build APK`** dans la colonne de gauche.
4. Cliquez sur le bouton **`Run workflow`** > **`Run workflow`** (vert).
5. GitHub lance la compilation gratuitement dans le cloud (durée : ~1 à 2 minutes).
6. Une fois terminé avec un coche vert ✅ :
   - Cliquez sur l'exécution du workflow.
   - En bas de la page dans la section **Artifacts**, téléchargez **`YouTube-Analytics-Debug-APK`**.
   - Vous obtenez votre fichier `.apk` prêt à être installé directement sur votre smartphone ou testé sur [Appetize.io](https://appetize.io) !

---

## 🛠️ Compilation en ligne de commande (Optionnel)

Si vous avez déjà Java 17 installé sur votre terminal :

```bash
# Sur Linux / macOS :
chmod +x ./gradlew
./gradlew assembleDebug

# Sur Windows :
gradlew.bat assembleDebug
```

Le fichier APK généré se trouvera dans : `app/build/outputs/apk/debug/app-debug.apk`.

---

## ✨ Fonctionnalités de l'application
- **Recherche de chaînes YouTube** par @identifiant, URL ou nom.
- **Tableau de bord statistique** : abonnés, vues globales, nombre de vidéos, ratio d'engagement.
- **Top 5 Vidéos** : analyse des vidéos les plus vues, les plus commentées et durées moyennes.
- **Comparateur de chaînes** : mise en compétition côte à côte de deux créateurs.
- **Multi-clés API & Gestion de quota** : rotation automatique et gestion des clés API YouTube v3.
- **Mode hors-ligne / Cache Room** : consultation instantanée de l'historique et des analyses précédentes sans connexion ni consommation de quota.
