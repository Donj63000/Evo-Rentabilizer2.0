# README – Outil de Rentabilité de Farm pour Dofus Rétro (Java)

## Introduction

Ce projet vise à fournir aux joueurs de **Dofus Rétro** un outil fiable et entièrement personnalisé permettant de mesurer la **rentabilité réelle** de leurs sessions de farm. L’objectif est de déterminer, pour chaque joueur, les **meilleures zones à farm** en fonction de sa manière de jouer, de son équipement, de son niveau, et du marché de son serveur.

## Concept du projet

L’application repose sur un principe simple :

1. Le joueur choisit une **zone à farm**.
2. Il lance une session de farm pendant une durée définie.
3. À la fin, il va en **HDV** et met en vente les ressources récoltées.
4. Il note la **valeur totale en kamas** générée par cette session (prix de vente HDV).
5. L’application enregistre :
   * La zone farmée
   * La durée de la session
   * La valeur en kamas
6. À partir de ces données, le logiciel calcule automatiquement la **rentabilité en kamas/heure**.
7. En répétant les sessions dans diverses zones, le joueur construit sa propre base de données et visualise :
   * Les zones les plus rentables
   * Les moyennes de kamas/heure
   * Les variations entre sessions
   * La fiabilité des résultats selon le nombre de sessions

## Objectif

Le but du logiciel n’est pas de donner des valeurs théoriques ou globales, mais plutôt de fournir une **analyse concrète, personnalisée et adaptée** au style de chaque joueur. Il aide à optimiser le temps de jeu en identifiant précisément les zones les plus rentables selon l’expérience réelle du joueur.

## Fonctionnalités principales (V1)

* Enregistrement de sessions de farm
* Calcul automatique du ratio kamas/heure
* Historique des sessions
* Agrégation par zone
* Classement des zones selon la rentabilité
* Stockage local des données

## Technologies

* **Langage** : Java
* **Type d’application** : application locale (desktop)
* **Stockage** : fichier local (selon implémentation : JSON, CSV ou SQLite)

## Public visé

Joueurs de Dofus Rétro souhaitant optimiser leur farm et disposer d’un outil statistique fiable, basé uniquement sur leurs données personnelles.

## État du projet

Documentation et conception en cours. Les premières itérations du développement viseront une version minimale permettant l’enregistrement de sessions et le calcul automatique de la rentabilité.

## MVP CLI

Les fonctionnalités minimales livrées dans cette première version en ligne de commande :

* Ajout d’une session (zone, durée en minutes, kamas totaux) avec calcul automatique du ratio K/h.
* Historisation locale des sessions dans une base SQLite embarquée (`~/.dofus-rentabilizer/data.db`).
* Agrégation par zone : nombre de sessions, temps cumulé, kamas cumulés, K/h moyen.
* Consultation rapide de l’historique.

## Stack et build

* **Langage / JDK** : Java 21 (compatibilité Java ≥17).
* **Build principal** : Gradle (Kotlin DSL) avec plugin Shadow pour générer un fat-jar.
* **Libs** : [Picocli](https://picocli.info/) pour le CLI, [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) pour le stockage.
* **Alternative temporaire** : un `pom.xml` est conservé pour compiler rapidement dans les environnements où Gradle n’est pas encore installé.

## Guide d’utilisation (CLI)

Depuis la racine du projet :

```bash
./gradlew run --args="add --zone 'Porcos' --minutes 38 --kamas 115000"
./gradlew run --args="stats"
./gradlew run --args="history -n 10"
```

Si vous utilisez Maven, le même binaire peut être lancé avec :

```bash
mvn -q -DskipTests compile exec:java -Dexec.mainClass="com.dofus.rentabilizer.Main" -Dexec.args="stats"
```

## Interface graphique Dofus 1.29

* Lancez simplement `java -jar build/libs/dofus-rentabilizer-all.jar` (ou exécutez la classe `com.dofus.rentabilizer.Main` sans arguments) pour ouvrir l’interface Swing inspirée de la charte Dofus 1.29.
* Au démarrage, seul un menu central propose les quatre choix (« Mode farm », « Informations zones », « Historique », « Options ») sous forme de cartes jade/or. Chaque sélection ouvre une page dédiée avec bouton « Retour ».
* L’onglet **Historique** liste vos 25 dernières sessions (zone, dates, durée, kamas, ratio K/h).
* L’onglet **Statistiques** présente les agrégations par zone (nombre de sessions, minutes, kamas, moyenne K/h).
* Le bouton « Mode farm » ouvre un formulaire pour ajouter une session directement depuis l’interface ; les tableaux se rafraîchissent automatiquement.

## Crédit

Ce logiciel est conçu pour la guilde **Evolution** sur le serveur **Boune** de Dofus Rétro, à qui il est dédié.

---
