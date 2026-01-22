package Commune.Dev.Models;

public enum AuditAction {
    CREATE_USER,
    UPDATE_NAME,
    UPDATE_EMAIL,
    CHANGE_ROLE,
    DISABLE_ACCOUNT,
    ENABLE_ACCOUNT,
    UPDATE_PROFILE,

    UPDATE_ASSIGNMENT,
    DELETE_ACCOUNT,
    // Actions pour la gestion des mots de passe
    RESET_PASSWORD,              // Réinitialisation réussie du mot de passe
    FAILED_PASSWORD_RESET,       // Tentative échouée de réinitialisation
    CHANGE_PASSWORD,            // Changement de mot de passe par l'utilisateur
    VALID_SESSION,
    VALID_PAIEMENT
}
