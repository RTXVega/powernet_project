# Réseau de distribution d'électricité.

**Fait par (TP du jeudi 13h30-16h30)**  
Almas KASSYMBEKOV  
Anis BOUHAIL  
Rui MA

## Structure du projet

```
powernet_project/
├─ src/main/java/powernet/
│  ├─ model/
│  │  ├─ Consumption.java   # niveaux de consommation (BASSE, NORMAL, FORTE)
│  │  ├─ House.java         # maison (id, niveau de consommation)
│  │  └─ Generator.java     # générateur (id, capacité en kW)
│  ├─ core/
│  │  ├─ Network.java           # représentation du réseau (maisons, générateurs, connexions)
│  │  ├─ NetworkValidator.java  # vérifications minimales (maisons/générateurs présents, maisons connectées)
│  │  ├─ CostCalculator.java    # calcul de la dispersion, de la surcharge et du coût total
│  │  ├─ NetworkPrinter.java    # affichage texte du réseau et des coûts
│  │  ├─ NetworkWriter.java     # sauvegarde du réseau au format texte
│  │  ├─ NetworkParser.java     # parsing des fichiers réseau
│  │  ├─ AutoSolver.java        # heuristique hybride (BFD + recuit)
│  │  └─ SolverTest.java        # utilitaire de test manuel
│  ├─ cli/
│  │  ├─ ConsoleMenu.java   # interface textuelle (menus, saisies utilisateur)
│  │  ├─ ConsoleMenuV2.java # interface textuelle alternative
│  │  └─ App.java           # point d’entrée de l’application
│  └─ fx/                   # interface graphique JavaFX
│     ├─ PowerNetApp.java
│     ├─ controller/        # contrôleurs d’écran
│     └─ view/              # composants graphiques (canvas, panneaux)
└─ src/test/java/powernet/ # tests unitaires
```

## Prérequis
- Maven installé et disponible via `mvn` (3.8+ recommandé)  
- macOS (Homebrew) : `brew install maven`  
- Linux (apt) : `sudo apt-get update && sudo apt-get install maven`

## Build/Run via Maven (sans jar manuel)
- CLI (recommandé) : `mvn -DskipTests clean compile exec:java -Dexec.mainClass=powernet.cli.App`
- JavaFX GUI : `mvn -DskipTests clean compile javafx:run`
- Build complet si besoin : `mvn -DskipTests clean package` (jar dans `target/`, mais l’usage principal reste via Maven)

## Tests unitaires
- À la racine du projet : `mvn test` (JaCoCo génère ensuite `target/site/jacoco/index.html`)
