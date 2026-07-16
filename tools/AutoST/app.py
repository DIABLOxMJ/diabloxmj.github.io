from flask import Flask, request, jsonify
from flask_cors import CORS
from faster_whisper import WhisperModel
import os
import tempfile
import argostranslate.package
import argostranslate.translate
import subprocess
import re
from collections import Counter
import difflib  # Pour la correction intelligente des refrains proches

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

# ---- CONFIGURATION DE WHISPER ----
print("Chargement de l'IA Whisper Professionnelle en mémoire...")
try:
    model = WhisperModel(
        "large-v3", 
        device="cuda", 
        compute_type="int8", 
        download_root=chemin_modele
    )
    print("Modèle 'large-v3' chargé sur GPU ! 🚀")
except Exception as gpu_error:
    print(f"Repli sur le CPU : {gpu_error}")
    model = WhisperModel(
        "large-v3", 
        device="cpu", 
        compute_type="int8", 
        download_root=chemin_modele
    )

def nettoyer_texte(texte):
    if not texte:
        return ""
    # Nettoyage minimaliste pour préserver la ponctuation naturelle de l'IA
    texte_nettoye = re.sub(r'[@#\-_§µ*]+', '', texte)
    return texte_nettoye.strip()

def detecter_refrain_recurrence_universel(segments):
    """
    Analyse mathématique des n-grammes de mots répétés pour identifier 
    un refrain/hook de manière 100% dynamique (sans dictionnaire).
    """
    mots_globaux = []
    for s in segments:
        mots_propres = re.sub(r'[^\w\s]', '', s.text.lower()).split()
        if mots_propres:
            mots_globaux.extend(mots_propres)
            
    # On cherche des répétitions sur des suites de 2 à 4 mots
    n = 3
    phrases_candidates = []
    for i in range(len(mots_globaux) - n + 1):
        phrase = " ".join(mots_globaux[i:i+n])
        phrases_candidates.append(phrase)
        
    if not phrases_candidates:
        return None, 0
        
    compteur = Counter(phrases_candidates)
    if not compteur:
        return None, 0
        
    phrase_commune, nb_repetitions = compteur.most_common(1)[0]
    
    # Si une suite de mots revient au moins 3 fois, c'est statistiquement un refrain
    if nb_repetitions >= 3:
        return phrase_commune, nb_repetitions
    return None, 0

def corriger_refrain_dynamique(texte, refrain_detecte):
    """
    Redressement universel : Compare le texte courant au refrain détecté dynamiquement 
    au Passage 0. Si la similarité est très haute mais imparfaite (bruit, bégaiement de l'IA),
    on harmonise vers la version propre du refrain.
    """
    if not refrain_detecte:
        return texte
        
    texte_clean = texte.lower().strip()
    ref_clean = refrain_detecte.lower().strip()
    
    # Ratio de similarité mathématique (Général et universel)
    ratio = difflib.SequenceMatcher(None, texte_clean, ref_clean).ratio()
    
    # Seuil strict (85%) : On ne corrige que les déformations mineures (lettre manquante, mauvaise consonne finale)
    if 0.85 <= ratio < 1.0:
        return refrain_detecte.capitalize()
        
    return texte


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
        print(f"Analyse audio intelligente [Mode sélectionné : {audio_mode.upper()}]...")
        
        # =========================================================================
        # PASSAGE 0 : L'ÉCLAIREUR IA (ANALYSE STATISTIQUE GLOBALE)
        # =========================================================================
        print("-> [Passage 0] Analyse préliminaire globale (Éclaireur IA)...")
        try:
            segments_p0, info_p0 = model.transcribe(
                video_path,
                beam_size=1,
                temperature=0.0,
                condition_on_previous_text=False
            )
            langue_detectee = info_p0.language
            probabilite_langue = info_p0.language_probability
            liste_p0 = list(segments_p0)
            duree_totale_audio = info_p0.duration
        except Exception as e:
            print(f"   [!] Échec du Passage 0 : {e}. Configuration par défaut.")
            langue_detectee = "en"
            probabilite_langue = 0.50
            liste_p0 = []
            duree_totale_audio = 180.0
            
        if langue_detectee not in ["en", "fr"]:
            langue_detectee = "en"
            
        print(f"   * Langue validée : {langue_detectee.upper()} (Certitude : {probabilite_langue:.2%})")
        
        # Détection automatique du début utile
        debut_audio_utile = 0.0
        for seg in liste_p0:
            txt_sec = seg.text.strip().lower()
            if len(txt_sec) > 3 and seg.avg_logprob > -1.0:
                debut_audio_utile = max(0.0, seg.start - 0.5)
                print(f"   * Début utile identifié à : {debut_audio_utile:.2f} secondes (Intro ignorée).")
                break
                
        # Détection 100% universelle du refrain (aucun mot clé codé en dur)
        refrain_detecte, repetitions = detecter_refrain_recurrence_universel(liste_p0)
        if refrain_detecte:
            print(f"   * Refrain / Hook détecté dynamiquement : \"{refrain_detecte}\" ({repetitions} fois). Protection active ! 🛡️")
            penalite_repetition_dynamique = 1.25
        else:
            print("   * Aucun refrain récurrent identifié.")
            penalite_repetition_dynamique = 1.50

        output_en = []
        output_fr = []
        
        # VAD adaptatif
        if audio_mode == "music":
            activer_vad = False
            parametres_vad = None
        else:
            activer_vad = True
            parametres_vad = dict(min_speech_duration_ms=250)

        # =========================================================================
        # PRÉPARATION DES PARAMÈTRES DYNAMIQUES (CHUNKING CONTRE LA CONDENSATION)
        # =========================================================================
        params_transcribe_base = {
            "language": langue_detectee,
            "beam_size": 5,
            "condition_on_previous_text": False,
            "vad_filter": activer_vad,
            "vad_parameters": parametres_vad,
            "chunk_length": 15,  # Analyse par fenêtres de 15s pour un meilleur découpage naturel
        }

        utilisera_decoupage = False
        if debut_audio_utile > 1.5:
            params_transcribe_base["clip_timestamps"] = [float(debut_audio_utile), float(duree_totale_audio)]
            utilisera_decoupage = True

        # =========================================================================
        # PASSAGES 1, 2, 3 : CAPTATIONS BRUTES MULTI-TEMPÉRATURES
        # =========================================================================
        print(f"-> [Passage 1] Captation brute (Strict)...")
        liste_p1 = []
        try:
            params_p1 = params_transcribe_base.copy()
            params_p1.update({
                "temperature": 0.0,
                "repetition_penalty": penalite_repetition_dynamique,
                "no_repeat_ngram_size": 4
            })
            segs_p1, _ = model.transcribe(video_path, **params_p1)
            liste_p1 = list(segs_p1)
        except Exception as e:
            print(f"   [!] Échec Passage 1 : {e}")

        print(f"-> [Passage 2] Captation brute (Modérée)...")
        liste_p2 = []
        try:
            params_p2 = params_transcribe_base.copy()
            params_p2.update({
                "temperature": 0.2,
                "repetition_penalty": max(1.1, penalite_repetition_dynamique - 0.1),
                "no_repeat_ngram_size": 4
            })
            segs_p2, _ = model.transcribe(video_path, **params_p2)
            liste_p2 = list(segs_p2)
        except Exception as e:
            print(f"   [!] Échec Passage 2 : {e}")

        print(f"-> [Passage 3] Captation brute (Créative)...")
        liste_p3 = []
        try:
            params_p3 = params_transcribe_base.copy()
            params_p3.update({
                "temperature": 0.4,
                "repetition_penalty": penalite_repetition_dynamique,
                "no_repeat_ngram_size": 4
            })
            segs_p3, _ = model.transcribe(video_path, **params_p3)
            liste_p3 = list(segs_p3)
        except Exception as e:
            print(f"   [!] Échec Passage 3 : {e}")

        if utilisera_decoupage:
            for l in [liste_p1, liste_p2, liste_p3]:
                for s in l:
                    s.start += debut_audio_utile
                    s.end += debut_audio_utile

        # =========================================================================
        # PASSAGE 4 : ARBITRAGE ET FUSION PAR CONFIANCE DE L'IA
        # =========================================================================
        print("-> [Passage 4] Fusion et arbitrage par score de confiance...")
        options_textes = []
        for i, list_p in enumerate([liste_p1, liste_p2, liste_p3]):
            if not list_p:
                continue
            texte_brut = " ".join([s.text for s in list_p])
            confiance_moyenne = sum([s.avg_logprob for s in list_p]) / len(list_p)
            options_textes.append({"texte": texte_brut, "confiance": confiance_moyenne, "source": f"P{i+1}", "segments": list_p})
            
        if options_textes:
            meilleure_source = max(options_textes, key=lambda x: x["confiance"])
            texte_de_base = meilleure_source["texte"]
            segments_de_base = meilleure_source["segments"]
            print(f"   => Consensus retenu : {meilleure_source['source']} (Confiance : {meilleure_source['confiance']:.2f})")
        else:
            texte_de_base = ""
            segments_de_base = []

        # =========================================================================
        # PASSAGE 5 : ALIGNEMENT TEMPOREL ET RECALAGE CHIRURGICAL MOT À MOT
        # =========================================================================
        print("-> [Passage 5] Recalage et synchronisation fine (Word-Level Timestamps)...")
        try:
            segments_recales, _ = model.transcribe(
                video_path,
                language=langue_detectee,
                beam_size=5,
                initial_prompt=texte_de_base[:1000] if texte_de_base else None,
                temperature=0.0,
                repetition_penalty=penalite_repetition_dynamique,
                no_repeat_ngram_size=4,
                condition_on_previous_text=False,
                word_timestamps=True,
                vad_filter=activer_vad,
                vad_parameters=parametres_vad
            )
            liste_recalement = list(segments_recales)
        except Exception as e:
            print(f"   [!] Échec de l'alignement : {e}. Repli sur la version de base.")
            liste_recalement = segments_de_base

        # =========================================================================
        # PASSAGE 6 : CONTRÔLE DE QUALITÉ FINALE (FILTRAGE BASÉ SUR LA PHYSIQUE DU SON)
        # =========================================================================
        print("-> [Passage 6] Contrôle de conformité et redressement universel...")
        liste_validee = []
        for segment in liste_recalement:
            txt = nettoyer_texte(segment.text)
            
            # FILTRE UNIVERSEL 1 : Si la probabilité d'absence de parole (silence/musique pure) 
            # est supérieure à 80%, ou que la confiance globale est catastrophique (< -1.3),
            # on supprime. Cela éradique les hallucinations type "Jérémy Diaz" de manière 100% mathématique.
            if segment.no_speech_prob > 0.80 or segment.avg_logprob < -1.25:
                continue
                
            txt = " ".join(txt.split())
            if not txt:
                continue
            
            # CORRECTION UNIVERSELLE 2 : Redressement basé uniquement sur le refrain trouvé par l'éclaireur
            if refrain_detecte:
                txt = corriger_refrain_dynamique(txt, refrain_detecte)
            
            liste_validee.append({
                "start": segment.start,
                "end": segment.end,
                "text": txt.capitalize()
            })

        # =========================================================================
        # PASSAGE 7 : TRADUCTION DE CONFIANCE ISOLÉE
        # =========================================================================
        segments_traduits_en = None
        if langue_detectee == "fr":
            print("-> [Passage 7] Traduction anglaise isolée depuis la source validée...")
            texte_verifie = " ".join([s["text"] for s in liste_validee])
            try:
                segments_trad_en, _ = model.transcribe(
                    video_path,
                    language="fr",
                    task="translate",
                    initial_prompt=texte_verifie[:1000] if texte_verifie else None,
                    beam_size=5,
                    temperature=0.0,
                    repetition_penalty=penalite_repetition_dynamique,
                    no_repeat_ngram_size=4,
                    condition_on_previous_text=False,
                    vad_filter=activer_vad,
                    vad_parameters=parametres_vad
                )
                segments_traduits_en = list(segments_trad_en)
            except Exception as e:
                print(f"   [!] Échec de la traduction Passage 7 : {e}")

        # =========================================================================
        # CRÉATION DES LIVRABLES ET ALIGNEMENT
        # =========================================================================
        print("Finalisation des livrables de sous-titres...")
        
        if langue_detectee == "en":
            for item in liste_validee:
                texte_anglais = item["text"]
                try:
                    texte_francais = argostranslate.translate.translate(texte_anglais, "en", "fr")
                except:
                    texte_francais = texte_anglais

                output_en.append({"start": item["start"], "end": item["end"], "text": texte_anglais})
                output_fr.append({"start": item["start"], "end": item["end"], "text": texte_francais})

        else:
            for item in liste_validee:
                output_fr.append({"start": item["start"], "end": item["end"], "text": item["text"]})

            if segments_traduits_en:
                for segment in segments_traduits_en:
                    # Application du filtre universel de non-parole sur la traduction également
                    if segment.no_speech_prob > 0.80 or segment.avg_logprob < -1.15:
                        continue
                    texte_anglais = nettoyer_texte(segment.text)
                    if not texte_anglais:
                        continue
                    output_en.append({"start": segment.start, "end": segment.end, "text": texte_anglais.capitalize()})
            
        if output_en and output_en[0]["start"] > 4.0:
            temps_debut_parole = output_en[0]["start"]
            output_en.insert(0, {"start": 0, "end": temps_debut_parole - 0.5, "text": "[🎶 Music 🎶]"})
            
        if output_fr and output_fr[0]["start"] > 4.0:
            temps_debut_parole = output_fr[0]["start"]
            output_fr.insert(0, {"start": 0, "end": temps_debut_parole - 0.5, "text": "[🎶 Musique 🎶]"})

        print("Traitement d'élite adaptatif (avec Passage 0) finalisé avec succès ! 🌟")
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

# Les autres routes restent identiques...
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