import os
import json

BASE_DIR_SCRIPT = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(BASE_DIR_SCRIPT)
PACK_DIR_SOURCE = os.path.join("Pack (Main)")
PACK_DIR = os.path.join(BASE_DIR, PACK_DIR_SOURCE, "assets", "minecraft")
OUTPUT_DIR = os.path.join(BASE_DIR, "IndexData")

ITEMS_DEF_DIR = os.path.join(PACK_DIR, "items")
ITEM_MODELS_DIR = os.path.join(PACK_DIR, "models", "item")
ALL_MODELS_DIR = os.path.join(PACK_DIR, "models")

def find_textures_in_item_model(model_path_or_name):
    """Fonction récursive pour extraire les textures d'un modèle d'item via son parent"""
    clean_name = model_path_or_name.split(":")[-1] if ":" in model_path_or_name else model_path_or_name
    
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

    if "textures" in data:
        textures.update(data["textures"])

    if "parent" in data:
        parent_textures = find_textures_in_item_model(data["parent"])
        for k, v in parent_textures.items():
            if k not in textures:
                textures[k] = v

    return textures

def extract_models_from_item_def(data):
    """Extrait les chemins/noms de modèles depuis un fichier de définition d'item (items/*.json)"""
    models = []
    
    def search_model(obj):
        if isinstance(obj, dict):
            if "model" in obj and isinstance(obj["model"], str):
                models.append(obj["model"])
            for v in obj.values():
                search_model(v)
        elif isinstance(obj, list):
            for item in obj:
                search_model(item)

    search_model(data)
    return list(dict.fromkeys(models))

def index_items():
    print("\n=== DÉBUT DE L'INDEXATION DES ITEMS ===")
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    index_result = []

    if os.path.exists(ITEMS_DEF_DIR) and len(os.listdir(ITEMS_DEF_DIR)) > 0:
        for filename in os.listdir(ITEMS_DEF_DIR):
            if not filename.endswith(".json"):
                continue
                
            item_id = filename.replace(".json", "")
            item_def_path = os.path.join(ITEMS_DEF_DIR, filename)
            
            try:
                with open(item_def_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
            except Exception as e:
                continue

            raw_models = extract_models_from_item_def(data)

            # --- NETTOYAGE ET DÉTECTION BLOC ---
            is_block = False
            filtered_models = []

            for m in raw_models:
                # Retirer le prefixe "minecraft:" s'il existe
                clean_ref = m.split(":")[-1] if ":" in m else m

                # Vérifier si c'est un modèle de bloc
                if clean_ref.startswith("block/") or clean_ref.startswith("models/block/"):
                    is_block = True
                else:
                    filtered_models.append(m)

            # METHODE 1 : Si tu veux TOUT EXCLURE ce qui pointe vers un bloc
            if is_block:
                print(f"[FILTRÉ] L'item '{item_id}' est ignoré car il utilise un modèle de Bloc ({raw_models})")
                continue

            # METHODE 2 : Si tu veux garder mais renomer (décommente ci-dessous et commente le 'if is_block:' du dessus)
            # btn_name = item_id.replace("_", " ").title() + (" (Bloc)" if is_block else "")

            combined_textures = {}
            model_paths = []

            for m_ref in filtered_models: # ou raw_models pour la méthode 2
                clean_m = m_ref.split(":")[-1] if ":" in m_ref else m_ref
                model_paths.append(f"{PACK_DIR_SOURCE}/assets/minecraft/models/{clean_m}.json")
                tex_found = find_textures_in_item_model(m_ref)
                combined_textures.update(tex_found)

            clean_textures = {}
            for key, val in combined_textures.items():
                if val.startswith("#"):
                    clean_textures[key] = val
                else:
                    clean_path = val.split(":")[-1] if ":" in val else val
                    clean_textures[key] = f"{PACK_DIR_SOURCE}/assets/minecraft/textures/{clean_path}.png"

            item_entry = {
                "id": item_id,
                "buttonName": item_id.replace("_", " ").title(), # ou btn_name pour méthode 2
                "item_definition": f"{PACK_DIR_SOURCE}/assets/minecraft/items/{filename}",
                "models": model_paths,
                "textures": clean_textures
            }
            index_result.append(item_entry)

    output_path = os.path.join(OUTPUT_DIR, "Index_Details_Items.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(index_result, f, indent=4, ensure_ascii=False)

    print(f"=== FIN DE L'INDEXATION : {len(index_result)} items enregistrés ===\n")

if __name__ == "__main__":
    index_items()