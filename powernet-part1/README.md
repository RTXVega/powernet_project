# PowerNet – PARTIE 1

## Utilisation (exemple)
```
=== PARTIE 1 — Construction du réseau (étape 1/2) ===
1) Ajouter un générateur (ex: G1 60)
2) Ajouter une maison (ex: M1 BASSE|NORMAL|FORTE)
3) Connecter maison et générateur (ex: M1 G1 ou G1 M1)
4) Terminer la saisie
> 1
entrée> G1 60
Ajouté: G1 (capacité=60kW)
> 2
entrée> M1 FORTE
Ajouté: M1 (FORTE)
> 3
entrée> M1 G1
Connecté: M1 -> G1
> 4
=== PARTIE 1 — Calculs et affichage (étape 2/2) ===
1) Calculer le coût
2) Modifier une connexion (ex: M1 G2)
3) Afficher le réseau
4) Quitter
> 1
GÉNÉRATEURS
 - G1: charge=40kW / capacité=60kW (r=0.667)

MAISONS
 - M1 (40 kW) -> G1

COÛTS
 Dispersion: 0.000000
 Surcharge:  0.000000
 TOTAL:      0.000000


Remarque: PARTIE 1 = pas de lecture/écriture de fichiers et pas d'algorithme d'optimisation.
