# Réseau de distribution d'électricité.

**Fait par (TP du jeudi 13h30-16h30)**  
Almas KASSYMBEKOV  
Anis BOUHAIL  
Rui MA

## Structure du projet

- powernet.model  
  - Consumption : niveaux de consommation (BASSE, NORMAL, FORTE)  
  - House : maison (id, niveau de consommation)  
  - Generator : générateur (id, capacité en kW)

- powernet.core  
  - Network : représentation du réseau (maisons, générateurs, connexions)  
  - NetworkValidator : vérifications minimales (maisons/générateurs présents, maisons connectées)  
  - CostCalculator : calcul de la dispersion, de la surcharge et du coût total  
  - NetworkPrinter : affichage texte du réseau et des coûts

- powernet.cli  
  - ConsoleMenu : interface textuelle (menus, saisies utilisateur)  
  - App : point d’entrée de l’application

---
## Tests unitaires
### Depuis la ligne de commande

Se placer à la racine du projet :

- pour la version de ligne de commande :

```bash
mvn test
```
---
## Utilisation


### Depuis la ligne de commande

Se placer à la racine du projet :

- pour la version de ligne de commande :

```bash
mvn -DskipTests exec:java -Dexec.mainClass=powernet.cli.App
```

- pour la version graphique :

```bash
mvn javafx:run
```

### Depuis un IDE

1. Importer le projet comme projet Java.  
2. Lancer la classe :  
   - powernet.cli.App
