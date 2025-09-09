# Configuration du Projet - Système de Gestion Communale

## 🔧 Configuration des Variables d'Environnement

Pour des raisons de sécurité, ce projet utilise des variables d'environnement pour stocker les informations sensibles.

### 1. Créer le fichier d'environnement

Copiez le fichier `.env.example` vers `.env` :

```bash
cp .env.example .env
```

### 2. Configurer les variables

Éditez le fichier `.env` avec vos vraies valeurs :

```bash
# Configuration de la base de données
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=votre_mot_de_passe_db

# Configuration email (pour les notifications)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre_email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_app

# Configuration JWT
JWT_SECRET=votre_clé_secrète_jwt_au_moins_32_caractères
JWT_EXPIRATION=86400000

# Configuration admin par défaut
ADMIN_USERNAME=admin
ADMIN_PASSWORD=mot_de_passe_admin_sécurisé
ADMIN_ROLE=ADMIN
```

### 3. Configuration Email Gmail

Pour utiliser Gmail :
1. Activez l'authentification à 2 facteurs
2. Générez un "Mot de passe d'application"
3. Utilisez ce mot de passe dans `MAIL_PASSWORD`

### 4. Démarrage

```bash
mvn spring-boot:run
```

## 🌐 Endpoints Disponibles

### Vérification de Commune
- **GET** `/api/commune-check`

Vérifie s'il existe une commune dans le système (le système ne permet qu'une seule commune).

Exemple :
```bash
curl "http://localhost:8080/api/commune-check"

# Réponse si une commune existe :
{
  "success": true,
  "message": "Une commune est déjà enregistrée dans le système",
  "data": true
}

# Réponse si aucune commune :
{
  "success": true,
  "message": "Aucune commune n'est encore enregistrée dans le système",
  "data": false
}
```

### Test d'Emails (Développement)
- **GET** `/api/email-test/temporary-password` - Test email mot de passe temporaire
- **GET** `/api/email-test/password-change` - Test email changement de mot de passe
- **GET** `/api/email-test/account-status` - Test email statut de compte
- **GET** `/api/email-test/async` - Test email asynchrone
- **GET** `/api/email-test/config` - Vérification configuration email

Exemple :
```bash
curl "http://localhost:8080/api/email-test/temporary-password?email=test@example.com&nom=Doe&prenom=John&password=temp123&role=DIRECTEUR"
```

## 📧 Système d'Emails

Le système envoie automatiquement des emails pour :

### Types d'Emails
- **Création d'utilisateur** : Mot de passe temporaire avec instructions
- **Changement de mot de passe** : Confirmation de sécurité
- **Modification de compte** : Notification des changements
- **Activation/Désactivation** : Changement de statut

### Fonctionnalités
- **Envoi asynchrone** : N'interrompt pas les opérations principales
- **Templates riches** : Emails formatés avec emojis et sections claires
- **Gestion d'erreurs** : Logging détaillé et récupération d'erreurs
- **Support multilingue** : Messages en français

### Configuration Email
Assurez-vous que vos variables d'environnement email sont correctement configurées :
```bash
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=votre_email@gmail.com
MAIL_PASSWORD=votre_mot_de_passe_app
```

## ⚠️ Sécurité

- **JAMAIS** commiter le fichier `.env`
- Utilisez des mots de passe forts
- Changez le `JWT_SECRET` en production
- **Mots de passe d'application Gmail** : Utilisez des mots de passe d'application, pas votre mot de passe principal
