import os
import json
import shutil
import sys

# 1. Chemins d'accès (S'adapte automatiquement à ton PC)
BASE_DIR = os.path.dirname(os.path.abspath(__file__)) # Racine du script
MINECRAFT_PATH = os.path.expandvars(r'G:\Minecraft')

# Récupération de la version depuis l'argument ou '1.18' par défaut
VERSION = sys.argv[1] if len(sys.argv) > 1 else '1.18'

INDEX_PATH = os.path.join(MINECRAFT_PATH, 'assets', 'indexes', f'{VERSION}.json')
OBJECTS_DIR = os.path.join(MINECRAFT_PATH, 'assets', 'objects')

# Dossier de sortie à la racine de là où se trouve le script
OUTPUT_DIR = os.path.join(BASE_DIR, 'Pack (Sound)')

print(f"Analyse de l'index de Minecraft (Version : {VERSION})...")
with open(INDEX_PATH, 'r') as f:
    data = json.load(f)

print("Extraction des sons et du fichier sounds.json en cours...")
compteur = 0

for path, info in data['objects'].items():
    # On extrait tout le dossier /sounds/ ET le fichier minecraft/sounds.json
    if 'sounds/' in path or path.endswith('sounds.json'):
        hash_val = info['hash']
        # Dans Minecraft, le dossier est constitué des 2 premiers caractères du hash
        source_file = os.path.join(OBJECTS_DIR, hash_val[:2], hash_val)
        dest_file = os.path.join(OUTPUT_DIR, path)

        # Crée les dossiers nécessaires et copie le fichier
        if os.path.exists(source_file):
            os.makedirs(os.path.dirname(dest_file), exist_ok=True)
            shutil.copy2(source_file, dest_file)
            compteur += 1

print(f"Terminé ! {compteur} fichiers audio et de configuration (sounds.json inclus) ont été extraits dans '{OUTPUT_DIR}' !")