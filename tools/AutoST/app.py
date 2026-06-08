from flask import Flask, request, jsonify
from flask_cors import CORS
from faster_whisper import WhisperModel
import os
import tempfile
import argostranslate.package
import argostranslate.translate
import subprocess

app = Flask(__name__)
CORS(app)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
chemin_modele = os.path.join(BASE_DIR, "modele_ia")

# ---- CONFIGURATION DU TRADUCTEUR LOCAL ----
print("Configuration du traducteur local (English -> Français)...")
try:
    argostranslate.package.update_package_index()
    available_packages = argostranslate.package.get_available_packages()
    package_to_install = next(
        filter(
            lambda x: x.from_code == "en" and x.to_code == "fr", available_packages
        )
    )
    argostranslate.package.install_from_path(package_to_install.download())
    print("Traducteur configuré et prêt !")
except Exception as e:
    print(f"Note : Chargement du traducteur hors-ligne (ou déjà installé).")

# ---- CONFIGURATION DE WHISPER HAUTE PRÉCISION ----
print("Chargement de l'IA Whisper Professionnelle en mémoire...")
model = WhisperModel("medium", device="cpu", compute_type="int8", download_root=chemin_modele)
print("Tout est prêt et opérationnel !")

@app.route('/transcribe', methods=['POST'])
def transcribe_video():
    if 'video' not in request.files:
        return jsonify({"error": "Pas de fichier vidéo reçu"}), 400
        
    video_file = request.files['video']
    
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(video_file.filename)[1]) as temp_video:
        video_file.save(temp_video.name)
        video_path = temp_video.name

    try:
        print("Analyse de la vidéo avec filtres anti-bruit et détection de voix...")
        
        # Configuration VAD standard, robuste et compatible avec toutes les versions
        segments, info = model.transcribe(
            video_path, 
            beam_size=5,
            vad_filter=True,
            vad_parameters=dict(min_speech_duration_ms=250)
        )
        
        output_en = []
        output_fr = []
        
        print("Traduction des séquences en cours...")
        for segment in segments:
            texte_anglais = segment.text.strip()
            
            if not texte_anglais or segment.avg_logprob < -1.0:
                continue
                
            texte_francais = argostranslate.translate.translate(texte_anglais, "en", "fr")
            
            output_en.append({
                "start": segment.start,
                "end": segment.end,
                "text": texte_anglais
            })
            
            output_fr.append({
                "start": segment.start,
                "end": segment.end,
                "text": texte_francais
            })
            
        print("Transcription et traduction réussies !")
        return jsonify({
            "fr": output_fr,
            "en": output_en
        })

    except Exception as e:
        print(f"Une erreur est survenue : {e}")
        return jsonify({"error": str(e)}), 500

    finally:
        if os.path.exists(video_path):
            try:
                os.remove(video_path)
            except:
                pass

@app.route('/save_zip', methods=['POST'])
def save_zip():
    if 'zip' not in request.files:
        return jsonify({"error": "Aucun fichier ZIP reçu"}), 400
        
    zip_file = request.files['zip']
    dossier_sauvegarde = os.path.join(BASE_DIR, "stock_srt")
    os.makedirs(dossier_sauvegarde, exist_ok=True)
    chemin_final = os.path.join(dossier_sauvegarde, zip_file.filename)
    zip_file.save(chemin_final)
    return jsonify({"status": "Sauvegardé avec succès dans 'stock_srt' ! 💾"})

@app.route('/open_folder', methods=['POST'])
def open_folder():
    dossier_sauvegarde = os.path.join(BASE_DIR, "stock_srt")
    os.makedirs(dossier_sauvegarde, exist_ok=True)
    try:
        os.startfile(dossier_sauvegarde)
        return jsonify({"status": "Dossier ouvert !"})
    except AttributeError:
        try:
            if os.name == 'posix':
                subprocess.Popen(['open', dossier_sauvegarde] if sys.platform == 'darwin' else ['xdg-open', dossier_sauvegarde])
            return jsonify({"status": "Dossier ouvert !"})
        except Exception as e:
            return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='localhost', port=5000)