import os
import json
import subprocess
import platform
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")

@app.route('/run/<script_type>', methods=['POST'])
def run_script(script_type):
    scripts = {
        'blocks': 'blockindexer.py',
        'items': 'itemindexer.py',
        'sounds': 'soundextractor.py',
        'categories': 'categoryindexer.py'
    }
    
    if script_type not in scripts:
        return jsonify({"status": "error", "message": "Script inconnu"}), 400
        
    script_name = scripts[script_type]
    script_path = os.path.join(BASE_DIR, script_name)
    
    if not os.path.exists(script_path):
        return jsonify({"status": "error", "message": f"Le fichier {script_name} est introuvable."}), 404

    try:
        result = subprocess.run(['py', script_path], capture_output=True, text=True, check=True)
        return jsonify({
            "status": "success", 
            "message": f"Le script {script_name} s'est exécuté avec succès !",
            "output": result.stdout
        })
    except subprocess.CalledProcessError as e:
        print(f"❌ Erreur lors du script : \n{e.stderr}") 
        return jsonify({
            "status": "error", 
            "message": f"Erreur lors de l'exécution de {script_name}.",
            "error": e.stderr
        }), 500

@app.route('/get-index/<type>', methods=['GET'])
def get_index(type):
    filename = "blocks.json" if type == "blocks" else "items.json"
    file_path = os.path.join(OUTPUT_DIR, filename)
    
    if not os.path.exists(file_path):
        return jsonify([])
        
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
        return jsonify(data)
    except Exception as e:
        print(f"❌ Erreur lors de la lecture de {filename} : {e}")
        return jsonify({"error": str(e)}), 500

@app.route('/get-categories', methods=['GET'])
def get_categories():
    file_path = os.path.join(OUTPUT_DIR, "categories.json")
    if not os.path.exists(file_path):
        return jsonify({})
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    return jsonify(data)

# Outil 1 : Ouvrir un fichier avec le programme par défaut (VSCode/Notepad++)
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
        elif platform.system() == "Darwin": # Mac
            subprocess.run(["open", full_path])
        else: # Linux
            subprocess.run(["xdg-open", full_path])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# Outil 2 : Ouvrir l'explorateur Windows et sélectionner le fichier
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
            # /select, permet d'ouvrir le dossier ET de surligner le fichier automatiquement !
            subprocess.run([f'explorer.exe', '/select,', full_path])
        else:
            # Pour Mac/Linux, on ouvre juste le dossier parent par simplicité
            parent_dir = os.path.dirname(full_path)
            if platform.system() == "Darwin":
                subprocess.run(["open", parent_dir])
            else:
                subprocess.run(["xdg-open", parent_dir])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    print("🚀 Le mini-serveur MCPackIndex est lancé sur http://localhost:5000")
    app.run(port=5000, debug=True)