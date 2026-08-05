import os
import json

BASE_DIR_SCRIPT = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(BASE_DIR_SCRIPT)
PACK_DIR_SOURCE = os.path.join("Pack (Main)")
PACK_DIR = os.path.join(BASE_DIR, PACK_DIR_SOURCE, "assets", "minecraft")
OUTPUT_DIR = os.path.join(BASE_DIR, "IndexData")
BLOCKSTATES_DIR = os.path.join(PACK_DIR, "blockstates")
MODELS_DIR = os.path.join(PACK_DIR, "models")

def find_textures_in_model(model_name):
    """Fonction récursive pour trouver les textures en suivant l'héritage"""
    if ":" in model_name:
        model_name = model_name.split(":")[1]
        
    model_path = os.path.join(MODELS_DIR, f"{model_name}.json")
    
    if not os.path.exists(model_path):
        return {}

    with open(model_path, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError:
            return {}

    textures = {}
    if "textures" in data:
        textures.update(data["textures"])
        
    if "parent" in data:
        parent_textures = find_textures_in_model(data["parent"])
        for k, v in parent_textures.items():
            if k not in textures:
                textures[k] = v
                
    return textures

def extract_models_from_variant(variant_data):
    """Extrait tous les modèles d'une variante (soit un dictionnaire, soit une liste de dictionnaires)"""
    models = []
    if isinstance(variant_data, list):
        for item in variant_data:
            if isinstance(item, dict) and "model" in item:
                models.append(item["model"])
    elif isinstance(variant_data, dict) and "model" in variant_data:
        models.append(variant_data["model"])
    return models

def index_blocks():
    print("\n=== DÉBUT DE L'INDEXATION DES BLOCS ===")
    
    if not os.path.exists(BLOCKSTATES_DIR):
        print(f"[ERROR] Le dossier {BLOCKSTATES_DIR} n'existe pas !")
        return

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    index_result = []
    
    files = [f for f in os.listdir(BLOCKSTATES_DIR) if f.endswith(".json")]

    for filename in files:
        block_id = filename.replace(".json", "")
        blockstate_path = os.path.join(BLOCKSTATES_DIR, filename)
        
        print(f"[ANALYSE] Bloc : {block_id} ...", end="")

        try:
            with open(blockstate_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
        except Exception as e:
            print(f"\n  [ERROR] Erreur de lecture : {e}")
            continue

        raw_models = []

        try:
            # 1. Analyse des "variants"
            if "variants" in data and isinstance(data["variants"], dict):
                for variant_name, variant_content in data["variants"].items():
                    raw_models.extend(extract_models_from_variant(variant_content))
            
            # 2. Analyse des "multipart"
            if "multipart" in data and isinstance(data["multipart"], list):
                for part in data["multipart"]:
                    if isinstance(part, dict) and "apply" in part:
                        raw_models.extend(extract_models_from_variant(part["apply"]))

        except Exception as e:
            print(f"\n  [ERROR] Structure imprévue pour {filename} : {e}")
            continue

        # Suppression des doublons tout en gardant l'ordre original
        unique_models = list(dict.fromkeys(raw_models))

        # Récupération de toutes les textures combinées de tous les modèles
        combined_textures = {}
        model_paths = []

        for m_ref in unique_models:
            clean_m = m_ref.split(":")[-1] if ":" in m_ref else m_ref
            model_paths.append(f"{PACK_DIR_SOURCE}/assets/minecraft/models/{clean_m}.json")
            
            # Extraction des textures pour ce modèle
            tex_found = find_textures_in_model(m_ref)
            combined_textures.update(tex_found)

        # Nettoyage des chemins de textures
        clean_textures = {}
        for key, val in combined_textures.items():
            # Si val est un dictionnaire (ex: objets de textures avancées), on extrait le chemin si possible
            if isinstance(val, dict):
                val = val.get("texture") or val.get("image") or ""

            # S'assurer que val est une chaîne de caractères non vide
            if isinstance(val, str) and val:
                if val.startswith("#"):
                    clean_textures[key] = val
                else:
                    clean_path = val.split(":")[-1] if ":" in val else val
                    clean_textures[key] = f"{PACK_DIR_SOURCE}/assets/minecraft/textures/{clean_path}.png"

        block_entry = {
            "id": block_id,
            "buttonName": block_id.replace("_", " ").title(),
            "blockstate": f"{PACK_DIR_SOURCE}/assets/minecraft/blockstates/{filename}",
            "models": model_paths,  # Nouvelle liste de chemins de modèles
            "textures": clean_textures
        }
        index_result.append(block_entry)
        print(" OK")

    output_path = os.path.join(OUTPUT_DIR, "Index_Details_Blocks.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(index_result, f, indent=4, ensure_ascii=False)
        
    print(f"=== FIN DE L'INDEXATION : {len(index_result)} blocs enregistrés ===\n")

if __name__ == "__main__":
    index_blocks()