const fs = require('fs'); //[cite: 41] Importe le module natif 'fs' (File System) de Node.js pour interagir avec le système de fichiers.
const path = require('path'); //[cite: 41] Importe le module natif 'path' pour manipuler les chemins de fichiers/dossiers de façon portable (Windows/Linux/Mac).

const BASE_DIR = path.dirname(__dirname); //[cite: 41] Récupère le chemin absolu du dossier parent du dossier courant (__dirname représentant le dossier 'scripts').
const CONFIG_FILE_PATH = path.join(BASE_DIR, 'config.json'); //[cite: 41] Forge le chemin absolu vers 'config.json' en le joignant au dossier de base.

/**
 * Fonction récursive qui résout tous les chemins relatifs d'un objet en chemins absolus.[cite: 41]
 */
function resolvePaths(pathsObj, baseDir) { //[cite: 41] Déclaration de la fonction prenant l'objet des chemins et le dossier de base.
	const resolved = {}; //[cite: 41] Initialise un nouvel objet vide pour stocker les chemins résolus.
	for (const [key, value] of Object.entries(pathsObj)) { //[cite: 41] Parcourt chaque paire [clé, valeur] du dictionnaire pathsObj.
		if (typeof value === 'string') { //[cite: 41] Si la valeur est une chaîne de caractères (un chemin relatif ou absolu)...
			resolved[key] = path.isAbsolute(value) ? value : path.join(baseDir, value); //[cite: 41] Si c'est déjà absolu, on conserve, sinon on le joint à baseDir.
		} else if (typeof value === 'object' && value !== null) { //[cite: 41] Si la valeur est un sous-objet (ex: PATHS.Indexer)...
			resolved[key] = resolvePaths(value, baseDir); //[cite: 41] Appel récursif pour traiter les sous-niveaux d'imbrication.
		} else {
			resolved[key] = value; //[cite: 41] Pour toute autre donnée (nombre, booléen), copie la valeur telle quelle.
		}
	}
	return resolved; //[cite: 41] Renvoie l'objet entièrement résolu.
}

function loadConfig() { //[cite: 41] Fonction lisant le fichier JSON et retournant la configuration préparée.
	if (!fs.existsSync(CONFIG_FILE_PATH)) { //[cite: 41] Vérifie si le fichier config.json existe sur le disque.
		throw new Error(`Fichier de configuration introuvable : ${CONFIG_FILE_PATH}`); //[cite: 41] Interrompt l'exécution en levant une exception si inexistant.
	}

	const rawData = fs.readFileSync(CONFIG_FILE_PATH, 'utf-8'); //[cite: 41] Lit le contenu textuel du fichier JSON en codage UTF-8.
	const jsonConfig = JSON.parse(rawData); //[cite: 41] Convertit la chaîne JSON en objet JavaScript manipulable.

	return { //[cite: 41] Renvoie un objet structuré contenant la configuration globale du projet.
		BASE_DIR: BASE_DIR, //[cite: 41] Chemin absolu racine.
		PORT: process.env.PORT || jsonConfig.PORT || 5000, //[cite: 41] Port du serveur (Priorité : variable d'environnement > JSON > 5000 par défaut).
		MINECRAFT_PATH: process.env.MINECRAFT_PATH || jsonConfig.MINECRAFT_PATH, //[cite: 41] Chemin d'accès Minecraft.
		UI: jsonConfig.UI || {}, //[cite: 41] Paramètres d'interface utilisateur.
		PATHS: resolvePaths(jsonConfig.PATHS || {}, BASE_DIR), //[cite: 41] Transforme tous les chemins déclarés en chemins absolus.
		FILES: jsonConfig.FILES || {}, //[cite: 41] Noms des fichiers d'indexation cibles.
		reload: loadConfig // Attache la fonction de rechargement à l'objet
	};
}

module.exports = loadConfig(); //[cite: 41] Exécute loadConfig() à l'importation (rend le résultat disponible directement via require).
//module.exports = loadConfig; //[cite: 41] Attache la fonction elle-même sous la clé .reload pour permettre le rechargement à chaud.