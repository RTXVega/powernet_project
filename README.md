# Réseau de distribution d'électricité.

**Fait par (TP du jeudi 13h30-16h30)**  
Almas KASSYMBEKOV  
Anis BOUHAIL  
Rui MA

## Structure du projet

```
powernet_project/
├─ README.md              # guide du projet et commandes
├─ ALGORITHME.md          # présentation détaillée de l'algorithme
├─ instances/             # fichiers d'instances d'entrée
├─ pom.xml                # configuration Maven
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
│  │  └─ AutoSolver.java        # heuristique hybride (BFD + recuit)
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
- JDK 17 ou supérieur installé (`java -version` pour vérifier)  
- Maven installé et disponible via `mvn` (3.8+ recommandé)  

### Installation de Maven
- macOS (Homebrew) : `brew install maven`  
- Linux (apt) : `sudo apt-get update && sudo apt-get install maven`

## Compiler et exécuter un fichier .jar (Mode CLI uniquement)
1. Dans la racine, exécuter `mvn -DskipTests clean package` pour complier.
2. Exécuter `java -jar target/powernet-2.0.0.jar ./instances/instance1.txt --lambda 10` (par exemple pour ./instances/instance1.txt avec lambda 10)

## Compiler et exécuter via Maven (Mode CLI et GUI)
- CLI : Exécuter sans argument `mvn -DskipTests clean compile exec:java -Dexec.mainClass=powernet.cli.App`
ou avec les instances chargé 
`mvn -DskipTests clean compile exec:java -Dexec.mainClass=powernet.cli.App -Dexec.args="instances/instance1.txt --lambda 10"`
- JavaFX GUI : `mvn -DskipTests clean compile javafx:run`

## Présentation de l'algorithme
- L'heuristique construit une solution initiale en plaçant d'abord les maisons les plus gourmandes sur les générateurs les moins chargés.
- Un recuit simulé affine ensuite la répartition en déplaçant ou échangeant des maisons, avec une température décroissante pour éviter les optima locaux.
- Détails complets : voir `ALGORITHME.md`.

## Tests unitaires
- À la racine du projet : `mvn clean test` (JaCoCo génère ensuite `target/site/jacoco/index.html`)
