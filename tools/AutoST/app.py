from flask import Flask, request, jsonify
from flask_cors import CORS
from faster_whisper import WhisperModel
import os
import tempfile
import argostranslate.package
import argostranslate.translate
import subprocess
import re

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

def nettoyer_texte(texte):
    """Nettoyage de sécurité basique pour éliminer les résidus de symboles"""
    if not texte:
        return ""
    # Supprime les symboles bizarres que l'IA génère parfois dans les blancs
    texte_nettoye = re.sub(r'[@#\-_§µ*]+', '', texte)
    return texte_nettoye.strip()

@app.route('/transcribe', methods=['POST'])
def transcribe_video():
    if 'video' not in request.files:
        return jsonify({"error": "Pas de fichier vidéo reçu"}), 400
        
    video_file = request.files['video']
    audio_mode = request.form.get('audio_mode', 'standard')
    
    with tempfile.NamedTemporaryFile(delete=False, suffix=os.path.splitext(video_file.filename)[1]) as temp_video:
        video_file.save(temp_video.name)
        video_path = temp_video.name

    try:
        print(f"Analyse audio intelligente [Mode séléctionné : {audio_mode.upper()}]...")
        
        # 1. SCAN DE LA LANGUE INITIAL (Focalisé uniquement sur EN et FR)
        try:
            segments_test, info_test = model.transcribe(video_path, beam_size=1, max_initial_timestamp=45.0)
            langue_detectee = info_test.language
            probabilite_langue = info_test.language_probability
        except Exception as e:
            print(f"Note sur la pré-analyse : {e}. Configuration par défaut en Anglais.")
            langue_detectee = "en"
            probabilite_langue = 0.50
            
        # FILET DE SÉCURITÉ STRICT : On ne tolère QUE le français ou l'anglais
        if langue_detectee not in ["en", "fr"]:
            print(f"-> Langue détectée non gérée ({langue_detectee}). Verrouillage automatique sur l'ANGLAIS.")
            langue_detectee = "en"
        else:
            print(f"-> Piste linguistique validée : {langue_detectee.upper()} (Certitude : {probabilite_langue:.2%})")
        
        output_en = []
        output_fr = []
        
        # 2. CONFIGURATION DE LA TRANSCRIPTION ADAPTATIVE
        print(f"-> Lancement de la transcription complète en {langue_detectee.upper()}...")
        
        if audio_mode == "music":
            activer_vad = False
            parametres_vad = None
            print("-> Filtre anti-bruit VAD désactivé pour la musique.")
        else:
            activer_vad = True
            parametres_vad = dict(min_speech_duration_ms=250)
        
        segments, info = model.transcribe(
            video_path, 
            beam_size=5,
            language=langue_detectee, # Forcé en 'en' ou 'fr'
            vad_filter=activer_vad,
            vad_parameters=parametres_vad
        )
        
        print("Traduction et alignement des pistes...")
        for segment in segments:
            texte_original = nettoyer_texte(segment.text)
            
            if not texte_original or segment.avg_logprob < -1.0:
                continue
                
            if len(texte_original) <= 1 and texte_original.lower() not in ["a", "i", "y"]:
                continue
            
            texte_anglais = texte_original
            texte_francais = texte_original
            
            # CAS A : LA VIDÉO/CHANSON EST EN ANGLAIS
            if langue_detectee == "en":
                texte_anglais = texte_original
                try:
                    texte_francais = argostranslate.translate.translate(texte_anglais, "en", "fr")
                except:
                    texte_francais = texte_original
                    
            # CAS B : LA VIDÉO/CHANSON EST EN FRANÇAIS
            else:
                texte_francais = texte_original
                try:
                    texte_anglais = argostranslate.translate.translate(texte_francais, "fr", "en")
                except:
                    texte_anglais = texte_original
            
            output_en.append({"start": segment.start, "end": segment.end, "text": texte_anglais})
            output_fr.append({"start": segment.start, "end": segment.end, "text": texte_francais})
            
        # 3. CRÉATION DU MEUBLAGE MUSICAL AUTOMATIQUE
        if output_en and output_en[0]["start"] > 4.0:
            temps_debut_parole = output_en[0]["start"]
            output_en.insert(0, {"start": 0, "end": temps_debut_parole - 0.5, "text": "[🎶 Music 🎶]"})
            output_fr.insert(0, {"start": 0, "end": temps_debut_parole - 0.5, "text": "[🎶 Musique 🎶]"})
            
        # Si le fichier ne contient aucun mot
        if not output_en:
            print("-> Aucun dialogue détecté. Mode instrumental pur.")
            output_en.append({"start": 0, "end": 15, "text": "[🎶 Instrumental Music 🎶]"})
            output_fr.append({"start": 0, "end": 15, "text": "[🎶 Musique Instrumentale 🎶]"})

        print("Traitement IA finalisé avec succès !")
        return jsonify({
            "fr": output_fr,
            "en": output_en
        })

    except Exception as e:
        print(f"Une erreur est survenue lors du traitement : {e}")
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
            import sys
            if os.name == 'posix':
                subprocess.Popen(['open', dossier_sauvegarde] if sys.platform == 'darwin' else ['xdg-open', dossier_sauvegarde])
            return jsonify({"status": "Dossier ouvert !"})
        except Exception as e:
            return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='localhost', port=5000)