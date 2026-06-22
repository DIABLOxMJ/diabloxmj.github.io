package com.diabloxmj.xpbank; // Déclare le package auquel appartient cette classe utilitaire

import net.minecraft.server.network.ServerPlayerEntity; // Importe la classe représentant un joueur connecté côté serveur

// Définition d'une classe "Utility" (boîte à outils). Elle ne s'instancie pas, ses méthodes sont statiques.
public class XpMathUtils {

    // Méthode calculant le nombre EXACT de points d'expérience (bruts) qu'un joueur possède au total
    public static int getPlayerTotalXp(ServerPlayerEntity player) {
        // Étape 1 : Calcule l'XP requise pour atteindre le niveau actuel du joueur via getXpForLevel().
        // Étape 2 : Calcule l'XP accumulée dans la barre de progression actuelle (pourcentage * XP requise pour le prochain niveau).
        // Étape 3 : Additionne et arrondit le tout au nombre entier le plus proche pour obtenir le total global.
        return getXpForLevel(player.experienceLevel) + Math.round(player.experienceProgress * player.getNextLevelExperience());
    }

    // Méthode contenant les équations officielles de Minecraft Vanilla pour convertir un niveau en points d'XP bruts
    public static int getXpForLevel(int level) {
        // Palier 1 : Du niveau 0 au niveau 16
        if (level <= 16) {
            // Formule Vanilla officielle : Niveau² + 6 × Niveau
            return level * level + 6 * level;
        }
        // Palier 2 : Du niveau 17 au niveau 31
        else if (level <= 31) {
            // Formule Vanilla officielle : 2.5 × Niveau² - 40.5 × Niveau + 360
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        // Palier 3 : À partir du niveau 32 et plus
        else {
            // Formule Vanilla officielle : 4.5 × Niveau² - 162.5 × Niveau + 2220
            return (int) (4.5 * level * level - 162.5 * level + 2220);
        }
    }

    // Méthode permettant de redéfinir précisément l'XP globale d'un joueur à partir d'un grand nombre de points bruts
    public static void setPlayerTotalXp(ServerPlayerEntity player, int totalXp) {
        // Réinitialise complètement les variables d'expérience du joueur à zéro pour reconstruire sa barre proprement
        player.experienceLevel = 0; // Remet le compteur de niveau visible à 0
        player.experienceProgress = 0.0F; // Vide complètement le pourcentage visuel de la barre d'XP (0.0 à 1.0)
        player.totalExperience = 0; // Réinitialise le compteur d'expérience totale natif de Minecraft

        int xpLeft = totalXp; // Crée une variable de calcul contenant la réserve de points à distribuer au joueur

        // Boucle "tant que" : Elle s'exécute en boucle tant qu'il reste des points d'XP à distribuer au joueur
        while (xpLeft > 0) {
            // Demande à Minecraft combien de points d'XP sont requis pour passer du niveau actuel au niveau supérieur
            int xpToNextLevel = player.getNextLevelExperience();

            // Si la réserve restante est suffisante pour remplir entièrement le niveau suivant
            if (xpLeft >= xpToNextLevel) {
                player.experienceLevel++; // Le joueur gagne un niveau supplémentaire
                xpLeft -= xpToNextLevel; // On retire le coût de ce niveau de notre réserve de points de calcul
            }
            // Si la réserve restante est inférieure à ce que requiert le niveau suivant (fin de la distribution)
            else {
                // Calcule le pourcentage restant (Points restants / Points totaux du niveau) et l'applique à la barre visuelle
                player.experienceProgress = (float) xpLeft / (float) xpToNextLevel;
                xpLeft = 0; // Force la variable à 0 pour arrêter immédiatement la boucle "while"
            }
        }
        // Enregistre définitivement la nouvelle valeur de points totaux dans la variable de suivi native de Minecraft
        player.totalExperience = totalXp;
    }
}