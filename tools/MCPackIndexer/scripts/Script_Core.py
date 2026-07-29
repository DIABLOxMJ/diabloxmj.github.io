import os
import json
import subprocess
import platform
import time
import shutil
from flask import Flask, jsonify, request
from flask import send_from_directory
from flask_cors import CORS

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

BASE_DIR_SCRIPT = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(BASE_DIR_SCRIPT)
OUTPUT_DIR = os.path.join(BASE_DIR, "IndexData")

@app.route('/run/<script_type>', methods=['POST'])
def run_script(script_type):
    scripts = {
        'blocks': 'Script_Block_Indexer.py',
        'items': 'Script_Item_Indexer.py',
        'sounds': 'Script_Sound_Extractor.py',
        'categories': 'Script_Category_Indexer.py',
        'comparaison': 'Script_Full_Indexer.py',
        'extract': 'Script_Extractor.py'
    }
    
    if script_type not in scripts:
        return jsonify({"status": "error", "message": "Script inconnu"}), 400
        
    script_name = scripts[script_type]
    script_path = os.path.join(BASE_DIR_SCRIPT, script_name)
    
    if not os.path.exists(script_path):
        return jsonify({"status": "error", "message": f"Le fichier {script_name} est introuvable."}), 404

    # On prépare la commande système
    cmd = ['py', script_path]

    # Si des données JSON ont été envoyées (comme la version pour les sons)
    data = request.get_json(silent=True) or {}
    if script_type == 'sounds' and 'version' in data:
        cmd.append(str(data['version']))

    try:
        # CORRECTION ICI : On passe la liste `cmd` complète à subprocess.run au lieu d'une liste fixe !
        result = subprocess.run(cmd, capture_output=True, text=True, check=True)
        return jsonify({
            "status": "success", 
            "message": f"Le script {script_name} s'est exécuté avec succès !\n" + result.stdout
        })
    except subprocess.CalledProcessError as e:
        return jsonify({
            "status": "error", 
            "message": f"Erreur lors de l'exécution de {script_name} : {e.stderr if e.stderr else e.output}"
        }), 500

@app.route('/get-index/<index_type>', methods=['GET'])
def get_index(index_type):
    files = {
        'blocks': 'Index_Details_Blocks.json',
        'items': 'Index_Details_Items.json'
    }
    if index_type not in files:
        return jsonify({"error": "Index inconnu"}), 400
        
    file_path = os.path.join(OUTPUT_DIR, files[index_type])
    if not os.path.exists(file_path):
        return jsonify([]), 200
        
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

@app.route('/get-categories', methods=['GET'])
def get_categories():
    file_path = os.path.join(OUTPUT_DIR, 'Index_Details_Category.json')
    if not os.path.exists(file_path):
        return jsonify({}), 200
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

# Nouvelle route pour récupérer le rapport de comparaison complet
@app.route('/get-Comparaison', methods=['GET'])
def get_Comparaison():
    file_path = os.path.join(OUTPUT_DIR, 'Index_Comparaison_Missing.json')
    if not os.path.exists(file_path):
        return jsonify([]), 200
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

@app.route('/open-file', methods=['POST'])
def open_file():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable : {full_path}"}), 404
        
    try:
        if platform.system() == "Windows":
            os.startfile(full_path)
        elif platform.system() == "Darwin":
            subprocess.run(["open", full_path])
        else:
            subprocess.run(["xdg-open", full_path])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/open-folder', methods=['POST'])
def open_folder():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable : {full_path}"}), 404
        
    try:
        if platform.system() == "Windows":
            subprocess.run([f'explorer.exe', '/select,', full_path])
        else:
            parent_dir = os.path.dirname(full_path)
            if platform.system() == "Darwin":
                subprocess.run(["open", parent_dir])
            else:
                subprocess.run(["xdg-open", parent_dir])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/read-file', methods=['POST'])
def read_file():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable"}), 404
        
    try:
        with open(full_path, 'r', encoding='utf-8') as f:
            content = f.read()
        return jsonify({"status": "success", "content": content})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/IndexData/<path:filename>')
def serve_index_data(filename):
    # OUTPUT_DIR est déjà défini comme os.path.join(BASE_DIR, "IndexData")
    return send_from_directory(OUTPUT_DIR, filename)

@app.route('/extract-single-file', methods=['POST'])
def extract_single_file():
    data = request.json or {}
    rel_path = data.get('path')
    
    if not rel_path:
        return jsonify({"status": "error", "message": "Aucun chemin fourni"}), 400

    # Retirer "Pack (Next)/assets/" si le chemin envoyé le contient déjà
    clean_path = rel_path.replace("Pack (Next)/assets/", "").replace("Pack (Main)/assets/", "")
    
    base_dir = os.path.dirname(os.path.abspath(__file__))
    src_file = os.path.normpath(os.path.join(base_dir, "Pack (Next)", "assets", clean_path))
    dest_file = os.path.normpath(os.path.join(base_dir, "Extract", "assets", clean_path))

    if not os.path.exists(src_file):
        return jsonify({"status": "error", "message": f"Fichier source introuvable dans Pack (Next) : {clean_path}"}), 404

    try:
        # Reconstitution exacte de l'arborescence dans Extract/assets/
        os.makedirs(os.path.dirname(dest_file), exist_ok=True)
        shutil.copy2(src_file, dest_file)
        return jsonify({"status": "success", "message": f"Fichier extrait avec succès dans {dest_file}"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)