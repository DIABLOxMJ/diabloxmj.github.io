import os
import json
import shutil

# Définition des dossiers
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
VANILLA_DIR = os.path.join(BASE_DIR, "Vanilla", "assets")
EXTRACT_DIR = os.path.join(BASE_DIR, "Extract", "assets")
REPORT_PATH = os.path.join(BASE_DIR, "finish", "comparison_report.json")

def run_extraction():
    print("\n=== DÉBUT DE L'EXTRACTION DES FICHIERS MANQUANTS ===")

    # 1. Vérifier si le rapport de comparaison existe
    if not os.path.exists(REPORT_PATH):
        print(" [ERREUR] Aucun rapport de comparaison trouvé. Lancez d'abord la comparaison.")
        return

    # 2. Lire le rapport de comparaison
    with open(REPORT_PATH, 'r', encoding='utf-8') as f:
        comparison_data = json.load(f)

    # 3. Filtrer pour obtenir uniquement les fichiers manquants (status == "missing")
    missing_files = [item['path'] for item in comparison_data if item.get('status') == 'missing']

    if not missing_files:
        print(" -> Aucun fichier manquant à extraire !")
        return

    print(f" -> {len(missing_files)} fichiers manquants identifiés.")

    # 4. Nettoyer/Créer le dossier d'extraction pour repartir sur du propre
    parent_extract_dir = os.path.dirname(EXTRACT_DIR) # Dossier "Extract" global
    if os.path.exists(parent_extract_dir):
        try:
            shutil.rmtree(parent_extract_dir)
        except Exception as e:
            print(f" [AVERTISSEMENT] Impossible de réinitialiser le dossier Extract : {e}")
    
    os.makedirs(EXTRACT_DIR, exist_ok=True)

    # 5. Copier les fichiers en gardant la structure
    compteur = 0
    erreurs = 0

    for rel_path in missing_files:
        src_file = os.path.normpath(os.path.join(VANILLA_DIR, rel_path))
        dest_file = os.path.normpath(os.path.join(EXTRACT_DIR, rel_path))

        if os.path.exists(src_file):
            # Crée les sous-dossiers nécessaires (ex: Extract/assets/minecraft/textures/block/)
            os.makedirs(os.path.dirname(dest_file), exist_ok=True)
            shutil.copy2(src_file, dest_file)
            compteur += 1
        else:
            erreurs += 1

    print(f"=== EXTRACTION TERMINÉE : {compteur} fichiers copiés dans 'Extract/assets/' ({erreurs} introuvables) ===\n")

if __name__ == "__main__":
    run_extraction()