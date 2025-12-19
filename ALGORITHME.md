# Fonctionnement de l'algorithme d'optimisation (PowerNet)

## Objectif

Trouver la meilleure répartition des maisons sur les générateurs :
- limiter les surcharges (éviter qu'un générateur saute) ;
- équilibrer la charge entre générateurs pour rester juste.

Au lieu de tester toutes les combinaisons possibles (beaucoup trop nombreuses), l'algorithme procède en deux temps, comme lorsqu'on range un coffre de voiture.

## 1. Démarrage malin (heuristique)

- On commence par les maisons les plus gourmandes ("gros cailloux") et on les place en premier.
- À chaque maison, on choisit le générateur qui a le plus de capacité libre (ou le taux de charge le plus bas) et on lui assigne la maison.
- On répète jusqu'à placer toutes les maisons.

On obtient ainsi une solution de base souvent "propre", mais rarement parfaite.

## 2. Peaufinage (recuit simulé)

On part de la solution heuristique et on la "secoue" pour l'améliorer, en s'inspirant du recuit des métaux.

- Une température démarre haute puis décroît progressivement.
- À chaque étape (environ 50 000), on tente une modification aléatoire :
  - déplacer une maison d'un générateur vers un autre ;
  - échanger deux maisons entre deux générateurs.
- Si le coût global s'améliore (moins de surcharge ou meilleur équilibre), on accepte la modification.
- Si le coût se dégrade, on peut tout de même accepter le mouvement, surtout quand la température est haute, pour éviter de rester bloqué dans un optimum local.
- Quand la température baisse, on devient plus exigeant ; en fin de course (froid), on n'accepte plus que les améliorations strictes et on fige la solution.

## Résumé

On range d'abord intelligemment les gros objets, puis on secoue l'ensemble (fort au début, doucement à la fin) pour que tout se cale au mieux. Ce mélange de logique et de hasard contrôlé produit rapidement de bonnes répartitions.
