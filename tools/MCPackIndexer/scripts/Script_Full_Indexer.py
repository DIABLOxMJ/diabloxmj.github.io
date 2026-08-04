import os
import json
import hashlib

BASE_DIR_SCRIPT = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(BASE_DIR_SCRIPT)
NEXT_DIR = os.path.join(BASE_DIR, "Pack (Next)", "assets")
MAIN_DIR = os.path.join(BASE_DIR, "Pack (Main)", "assets")
OUTPUT_DIR = os.path.join(BASE_DIR, "IndexData")

def scan_assets_folder(target_dir):
    """Scanne récursivement 100% des fichiers d'un dossier assets et retourne une liste de chemins relatifs"""
    file_list = []
    if not os.path.exists(target_dir):
        return file_list
        
    for root, dirs, files in os.walk(target_dir):
        for file in files:
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, target_dir).replace("\\", "/")
            file_list.append(rel_path)
    return sorted(file_list)

def compute_file_hash(file_path):
    """Calcule le hash MD5 d'un fichier. Pour le JSON, ignore les espaces/retour à la ligne."""
    if not os.path.exists(file_path):
        return None

    try:
        if file_path.endswith('.json') or file_path.endswith('.mcmeta'):
            with open(file_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
            # Reformatage compact sans espaces et clés triées pour comparaison pure du contenu
            clean_str = json.dumps(data, separators=(',', ':'), sort_keys=True)
            return hashlib.md5(clean_str.encode('utf-8')).hexdigest()
        else:
            # Pour les autres fichiers (images png, ogg, etc.), calcul binaire direct
            hasher = hashlib.md5()
            with open(file_path, 'rb') as f:
                buf = f.read(65536)
                while len(buf) > 0:
                    hasher.update(buf)
                    buf = f.read(65536)
            return hasher.hexdigest()
    except Exception:
        # En cas d'erreur de lecture/JSON malformé, fallback en lecture binaire
        try:
            hasher = hashlib.md5()
            with open(file_path, 'rb') as f:
                buf = f.read(65536)
                while len(buf) > 0:
                    hasher.update(buf)
                    buf = f.read(65536)
            return hasher.hexdigest()
        except Exception:
            return None

def run_comparaison():
    print("\n=== DÉBUT DE L'INDEXATION COMPLÈTE & COMPARAISON ===")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    print(f"[SCAN] Analyse du dossier Vanilla : {NEXT_DIR}...")
    next_files = scan_assets_folder(NEXT_DIR)
    with open(os.path.join(OUTPUT_DIR, "Index_NextPack.json"), "w", encoding="utf-8") as f:
        json.dump(next_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(next_files)} fichiers trouvés dans Vanilla.")

    print(f"[SCAN] Analyse du dossier personnel (Pack) : {MAIN_DIR}...")
    main_files = scan_assets_folder(MAIN_DIR)
    with open(os.path.join(OUTPUT_DIR, "Index_OldPack.json"), "w", encoding="utf-8") as f:
        json.dump(main_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(main_files)} fichiers trouvés dans ton Pack.")

    next_set = set(next_files)
    main_set = set(main_files)
    
    manquants = sorted(list(next_set - main_set))
    
    comparaison_result = []
    print("[COMPARAISON] Analyse des différences de contenu via Hash MD5...")
    
    for rel_path in next_files:
        parts = rel_path.split('/')
        category = parts[1] if len(parts) > 1 else "root"
        is_missing = rel_path in manquants
        has_content_diff = False

        # Si le fichier est présent dans les deux packs, on compare les Hashs
        if not is_missing:
            path_main = os.path.join(MAIN_DIR, rel_path)
            path_next = os.path.join(NEXT_DIR, rel_path)
            
            hash_main = compute_file_hash(path_main)
            hash_next = compute_file_hash(path_next)
            
            if hash_main and hash_next and hash_main != hash_next:
                has_content_diff = True
        
        comparaison_result.append({
            "path": rel_path,
            "filename": parts[-1],
            "category": category,
            "status": "missing" if is_missing else "present",
            "has_content_diff": has_content_diff
        })

    with open(os.path.join(OUTPUT_DIR, "Index_Comparaison_Missing.json"), "w", encoding="utf-8") as f:
        json.dump(comparaison_result, f, indent=4, ensure_ascii=False)

    diff_count = sum(1 for e in comparaison_result if e.get("has_content_diff"))
    print(f" -> Comparaison terminée : {len(manquants)} fichiers manquants, {diff_count} fichiers avec du contenu différent.")
    print("=== FIN DE L'INDEXATION COMPLÈTE ===\n")

if __name__ == "__main__":
    run_comparaison()