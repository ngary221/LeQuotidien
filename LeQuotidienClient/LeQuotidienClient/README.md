# Le•Quotidien — Application Cliente

Application Java/Swing de gestion des utilisateurs via le service SOAP du projet **Le•Quotidien**.

## Prérequis

| Outil   | Version minimale |
|---------|-----------------|
| Java    | 17+             |
| Maven   | 3.8+            |

> Aucune bibliothèque externe n'est requise. L'application utilise uniquement l'API Java SE standard.

---

## Compilation & exécution

```bash
# Cloner / se placer dans le répertoire
cd LeQuotidienClient

# Compiler et créer le JAR
mvn clean package

# Lancer l'application
java -jar target/le-quotidien-client.jar
```

---

## Utilisation

### 1. Écran de connexion

Au démarrage, l'application affiche un formulaire demandant :

| Champ         | Description                                              |
|---------------|----------------------------------------------------------|
| URL du serveur | Base URL du serveur Laravel, ex : `http://localhost/newsite/public` |
| Email          | Email de l'administrateur                                |
| Mot de passe   | Mot de passe associé                                     |

L'application appelle `authentifierUtilisateur(email, password)` via SOAP.

- Si l'authentification échoue → message d'erreur, connexion refusée.
- Si le rôle n'est pas `administrateur` → accès refusé.
- Sinon → saisie du **token API** (fourni séparément par l'administrateur du site via le back-office).

### 2. Tableau de bord

Une fois connecté, le tableau de bord affiche la liste complète des utilisateurs avec :

- **Recherche en temps réel** par nom, email ou rôle
- **Actualiser** : recharge la liste depuis le serveur
- **Ajouter** : formulaire de création d'un nouvel utilisateur
- **Modifier** (double-clic ou bouton) : modification des informations
- **Supprimer** : suppression avec confirmation

### 3. Déconnexion

Le bouton **Déconnexion** (en haut à droite) ferme le tableau de bord et retourne à l'écran de connexion.

---

## Architecture

```
src/main/java/com/lequotidien/
├── App.java                        ← Point d'entrée (main)
├── model/
│   ├── Utilisateur.java            ← Entité utilisateur
│   └── SoapResponse.java           ← Enveloppe de réponse SOAP
├── soap/
│   └── SoapClient.java             ← Client SOAP HTTP (sans stub généré)
├── ui/
│   ├── LoginFrame.java             ← Fenêtre de connexion
│   ├── MainFrame.java              ← Tableau de bord principal
│   └── UserFormDialog.java         ← Dialogue ajout / modification
└── util/
    └── Theme.java                  ← Charte graphique Swing
```

### Choix techniques

- **Pas de génération de stub WSDL** : les enveloppes SOAP sont construites manuellement en XML.  
  Avantage : zéro dépendance externe, compilation immédiate avec `javac` seul.
- **`SwingWorker`** pour tous les appels réseau : l'EDT (thread UI) n'est jamais bloqué.
- **Parsing JSON minimal** par regex : évite une dépendance sur Jackson/Gson pour un schéma très simple.

---

## Configuration du serveur

L'URL doit pointer vers la racine publique du projet Laravel, par exemple :

| Environnement | URL                                      |
|---------------|------------------------------------------|
| Local (XAMPP) | `http://localhost/newsite/public`        |
| Local (artisan serve) | `http://localhost:8000`         |
| Production    | `https://monserveur.com`                 |

Le WSDL est disponible en GET sur `<URL>/soap`.

---

## Erreurs courantes

| Message                                          | Cause                              |
|--------------------------------------------------|------------------------------------|
| Erreur de connexion : Connection refused         | Serveur non démarré                |
| Identifiants incorrects.                         | Email ou mot de passe erroné       |
| Token invalide ou expiré.                        | Token API incorrect                |
| Accès refusé. Seuls les administrateurs…         | Rôle insuffisant                   |
| Impossible de supprimer le seul administrateur.  | Protection système                 |
