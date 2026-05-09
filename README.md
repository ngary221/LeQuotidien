<div align="center">

# Le•Quotidien

**Projet d'Architecture Logicielle**

Application complète de gestion d'un journal en ligne, composée d'un site web Laravel,
de services web REST et SOAP, et d'une application cliente Java/Swing.

---

</div>

## Présentation

Le•Quotidien est une application de presse en ligne développée dans le cadre du cours
d'Architecture Logicielle. Elle est structurée en trois parties indépendantes et communicantes :

- **Site web** — interface publique de lecture et back-office d'administration (Laravel 12 + PostgreSQL)
- **Services web** — API REST (JSON/XML) et service SOAP pour l'accès programmatique aux données
- **Application cliente** — client Java/Swing permettant la gestion des utilisateurs via le service SOAP

---

## Architecture globale

```
┌─────────────────────────────────────────────────────────┐
│                     Le•Quotidien                        │
│                                                         │
│   ┌─────────────────────┐   ┌─────────────────────┐    │
│   │   Site Web Laravel  │   │  Application Java   │    │
│   │                     │   │       (Swing)        │    │
│   │  - Interface public │   │                     │    │
│   │  - Back-office      │   │  - Authentification │    │
│   │  - REST API         │◄──│  - Gestion users    │    │
│   │  - SOAP Service     │   │  - CRUD via SOAP    │    │
│   └─────────────────────┘   └─────────────────────┘    │
│            │                                            │
│     ┌──────▼──────┐                                     │
│     │ PostgreSQL  │                                     │
│     └─────────────┘                                     │
└─────────────────────────────────────────────────────────┘
```

---

## Structure du dépôt

```
LeQuotidien/
├── app/                          ← Logique métier Laravel
├── bootstrap/                    ← Initialisation Laravel
├── config/                       ← Configuration Laravel
├── database/
│   ├── migrations/               ← Structure de la base de données
│   └── seeders/                  ← Données initiales
├── public/                       ← Point d'entrée web
├── resources/views/              ← Vues Blade (interface publique + back-office)
├── routes/                       ← Définition des routes web et API
├── LeQuotidienClient/            ← Application cliente Java
│   ├── src/main/java/com/lequotidien/
│   │   ├── App.java              ← Point d'entrée
│   │   ├── model/                ← Entités métier
│   │   ├── soap/                 ← Client SOAP HTTP
│   │   ├── ui/                   ← Fenêtres Swing
│   │   └── util/                 ← Utilitaires (thème graphique)
│   ├── pom.xml                   ← Build Maven
│   └── README.md                 ← Documentation du client Java
├── DOCUMENTATION.txt             ← Documentation technique complète
├── CLIENT_DEVELOPER_GUIDE.txt    ← Guide d'intégration SOAP
└── README.md                     ← Ce fichier
```

---

## Partie 1 — Site Web (Laravel)

### Prérequis
- PHP 8.2+
- Composer
- PostgreSQL
- Extension PHP `soap` activée

### Installation

```bash
composer install
cp .env.example .env
php artisan key:generate
php artisan migrate --seed
php artisan storage:link
php artisan serve
```




### Fonctionnalités

**Interface publique**
- Page d'accueil avec pagination des articles
- Consultation d'un article en détail
- Navigation par catégorie

**Back-office** (après connexion)
- Gestion des articles et catégories (éditeurs + admins)
- Gestion des utilisateurs (admins uniquement)
- Génération de tokens API

---

## Partie 2 — Services Web

### REST API

Base URL : `/api`  
Authentification : en-tête `X-Api-Token` ou paramètre `?token=`

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/articles` | Liste de tous les articles publiés |
| GET | `/api/articles/categories` | Articles regroupés par catégorie |
| GET | `/api/articles/categorie/{slug}` | Articles d'une catégorie |

Ajouter `?format=xml` pour obtenir la réponse en XML (JSON par défaut).

### Service SOAP

Endpoint : `POST /soap`  
WSDL : `GET /soap`

| Méthode | Paramètres | Description |
|---|---|---|
| `authentifierUtilisateur` | email, password | Authentification (sans token) |
| `listerUtilisateurs` | token | Liste tous les utilisateurs |
| `ajouterUtilisateur` | token, nom, email, password, role | Création d'un utilisateur |
| `modifierUtilisateur` | token, id, nom, email, role | Modification d'un utilisateur |
| `supprimerUtilisateur` | token, id | Suppression d'un utilisateur |

---

## Partie 3 — Application Cliente Java

### Prérequis
- Java 17+

### Lancement

```bash
java -jar LeQuotidienClient/target/le-quotidien-client.jar
```

### Fonctionnement

```
Démarrage
    └─> Écran de connexion (URL serveur + email + mot de passe)
            └─> Authentification via SOAP
                    └─> Vérification rôle = "administrateur"
                            └─> Saisie du token API
                                    └─> Tableau de bord
                                            ├─> Lister les utilisateurs
                                            ├─> Ajouter un utilisateur
                                            ├─> Modifier un utilisateur
                                            └─> Supprimer un utilisateur
```

### Choix techniques
- **Zéro dépendance externe** : enveloppes SOAP construites manuellement en XML pur
- **Interface non bloquante** : tous les appels réseau exécutés via `SwingWorker`
- **Compatible Java 17+**

---

## Documentation

| Fichier | Contenu |
|---|---|
| `DOCUMENTATION.txt` | Documentation technique complète du projet |
| `CLIENT_DEVELOPER_GUIDE.txt` | Guide d'intégration du service SOAP |
| `LeQuotidienClient/README.md` | Documentation spécifique au client Java |

---

## Technologies utilisées

| Couche | Technologie |
|---|---|
| Backend | Laravel 12, PHP 8.2 |
| Base de données | PostgreSQL |
| Services web | REST (JSON/XML), SOAP (RPC/encoded) |
| Application cliente | Java 17, Swing |
| Build client | Maven 3.8 |
