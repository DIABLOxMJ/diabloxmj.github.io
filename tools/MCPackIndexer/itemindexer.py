import os
import json

# Chemins relatifs basés sur ta structure
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PACK_DIR = os.path.join(BASE_DIR, "Pack", "assets", "minecraft")
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")
ITEM_MODELS_DIR = os.path.join(PACK_DIR, "models", "item")
ALL_MODELS_DIR = os.path.join(PACK_DIR, "models") # Pour remonter les parents si besoin

def find_textures_in_item_model(model_path_or_name):
    """Fonction récursive pour extraire les textures d'un modèle d'item via son parent"""
    # Nettoyage du nom si format "minecraft:item/stone"
    clean_name = model_path_or_name.split(":")[-1] if ":" in model_path_or_name else model_path_or_name
    
    # On gère le fait que le chemin peut être absolu ou relatif à la racine des modèles
    if clean_name.endswith(".json"):
        full_path = clean_name
    else:
        full_path = os.path.join(ALL_MODELS_DIR, f"{clean_name}.json")

    if not os.path.exists(full_path):
        return {}

    with open(full_path, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError:
            return {}

    textures = {}

    # 1. On récupère les textures locales (souvent layer0, layer1, etc. pour les items)
    if "textures" in data:
        textures.update(data["textures"])

    # 2. On remonte chez le parent si nécessaire pour combler les manques
    if "parent" in data:
        parent_textures = find_textures_in_item_model(data["parent"])
        for k, v in parent_textures.items():
            if k not in textures:
                textures[k] = v

    return textures

def index_items():
    if not os.path.exists(ITEM_MODELS_DIR):
        print(f"Erreur : Le dossier {ITEM_MODELS_DIR} est introuvable. Vérifie le dossier /Pack.")
        return

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    index_result = []

    # On scanne le dossier des modèles d'items
    for filename in os.listdir(ITEM_MODELS_DIR):
        if filename.endswith(".json"):
            item_id = filename.replace(".json", "")
            model_path = os.path.join(ITEM_MODELS_DIR, filename)

            # On cherche les textures en partant de ce fichier de base
            textures = find_textures_in_item_model(model_path)

            # Nettoyage des chemins de textures pour l'interface web
            clean_textures = {}
            for key, val in textures.items():
                if val.startswith("#"): # Référence interne
                    clean_textures[key] = val
                else:
                    # Extraction du chemin propre sans le "minecraft:"
                    clean_path = val.split(":")[-1] if ":" in val else val
                    # Les textures d'items peuvent être dans textures/item/ ou textures/block/
                    clean_textures[key] = f"Pack/assets/minecraft/textures/{clean_path}.png"

            # Construction de l'objet pour l'index des items
            item_entry = {
                "id": item_id,
                "buttonName": item_id.replace("_", " ").title(),
                "model": f"Pack/assets/minecraft/models/item/{filename.split(':')[-1]}",
                "textures": clean_textures
            }
            index_result.append(item_entry)

    # Sauvegarde dans /finish/items.json
    output_path = os.path.join(OUTPUT_DIR, "items.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(index_result, f, indent=4, ensure_ascii=False)

    print(f"Indexation des items terminée ! {len(index_result)} items indexés dans /finish/items.json")

if __name__ == "__main__":
    index_items()