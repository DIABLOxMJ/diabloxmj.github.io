import os
import json
import shutil

# 1. Chemins d'accès (S'adapte automatiquement à ton PC)
MINECRAFT_PATH = os.path.expandvars(r'G:\Minecraft')
VERSION = '1.18'  # Remplace par ta version si besoin (ex: 1.20, 1.18.2)
INDEX_PATH = os.path.join(MINECRAFT_PATH, 'assets', 'indexes', f'{VERSION}.json')
OBJECTS_DIR = os.path.join(MINECRAFT_PATH, 'assets', 'objects')

# Dossier de sortie sur ton bureau
OUTPUT_DIR = os.path.join(os.path.expanduser('~'), 'Desktop', 'Minecraft_Sounds')

print("Analyse de l'index de Minecraft...")
with open(INDEX_PATH, 'r') as f:
    data = json.load(f)

print("Extraction des sons en cours...")
compteur = 0

for path, info in data['objects'].items():
    # On filtre pour ne prendre QUE les sons (on ignore les langues)
    if 'sounds/' in path:
        hash_val = info['hash']
        # Dans Minecraft, le dossier est les 2 premiers caractères du hash
        source_file = os.path.join(OBJECTS_DIR, hash_val[:2], hash_val)
        dest_file = os.path.join(OUTPUT_DIR, path)

        # Crée les dossiers (ex: /ui/, /block/, etc.) et copie le fichier
        if os.path.exists(source_file):
            os.makedirs(os.path.dirname(dest_file), exist_ok=True)
            shutil.copy2(source_file, dest_file)
            compteur += 1

print(f"Terminé ! {compteur} sons ont été extraits sur ton Bureau dans le dossier 'Minecraft_Sounds' !")
