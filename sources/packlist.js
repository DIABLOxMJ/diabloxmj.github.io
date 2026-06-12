// FONCTION COMMUNE POUR JOUER UN SON SANS LATENCE
function PlaySound(nomFichier, basePath = '') {
    const audio = new Audio(`${basePath}sources/${nomFichier}`);
    audio.volume = 0.25; // Volume à 25% pour rester discret et agréable
    audio.play().catch(err => console.log("Audio bloqué par le navigateur avant interaction."));
}

function initialiserTelechargements(containerId, packIdFilter = null, basePath = '') {
    // 1. Initialisation unique de la boîte modale de Changelog si absente
    if (!document.getElementById('changelog-modal')) {
        const modal = document.createElement('div');
        modal.id = 'changelog-modal';
        modal.className = 'fixed inset-0 bg-black/80 z-50 flex items-center justify-center opacity-0 pointer-events-none transition-opacity duration-300 backdrop-blur-sm px-4';
        modal.innerHTML = `
            <div id="changelog-box" class="bg-[#1a1a1a] border-2 border-neutral-700 w-full max-w-6xl p-6 rounded shadow-2xl relative text-white text-ombre max-h-[80vh] flex flex-col transform scale-95 transition-transform duration-300">
                <button id="close-changelog" class="absolute top-4 right-4 text-gray-400 hover:text-white font-bold text-2xl cursor-pointer leading-none">&times;</button>
                <h3 id="changelog-title" class="text-2xl font-bold mb-4 text-cyan-400 border-b border-neutral-700 pb-2 uppercase tracking-wide"></h3>
                <div id="changelog-content" class="text-lg leading-relaxed overflow-y-auto scroll-list text-justify pr-2 flex-grow font-medium whitespace-pre-wrap"></div>
            </div>
        `;
        document.body.appendChild(modal);

        const fermerModal = () => {
            const box = document.getElementById('changelog-box');
            PlaySound('click.mp3', basePath);
            modal.classList.remove('opacity-100', 'pointer-events-auto');
            modal.classList.add('opacity-0', 'pointer-events-none');
            box.classList.remove('scale-100');
            box.classList.add('scale-95');
        };
        modal.querySelector('#close-changelog').addEventListener('click', fermerModal);
        modal.addEventListener('click', (e) => { if (e.target === modal) fermerModal(); });
    }

    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = ''; 

    // Récupération dynamique du fichier packs.json
    fetch(`${basePath}sources/packlist.json`)
        .then(response => {
            if (!response.ok) throw new Error("Impossible de charger packlist.json");
            return response.json();
        })
        .then(data => {
            // Filtrage des données si un pack spécifique est demandé
            const viewer = packIdFilter ? data.filter(p => p.id === packIdFilter) : data;

            // Si on est sur une page de pack (ex: faithful.html), on remplit les textes et la galerie
            if (packIdFilter && viewer.length > 0) {
                const packInfo = viewer[0];
                injecterContenuPack(packInfo, basePath);
            }

            // Génération de l'affichage de la/les colonnes de téléchargements
            viewer.forEach(col => {
                const divColonne = document.createElement('div');
                divColonne.className = 'flex flex-col items-center w-full max-w-[260px] mx-auto';

                const Banner = document.createElement('img');
                Banner.src = basePath + col.banner;
                Banner.alt = col.title;
                Banner.className = 'border border-black w-full h-[100px] object-cover shadow-sm bg-gray-200';

                const VarList = document.createElement('div');
                const hauteurClass = packIdFilter ? 'h-[435px]' : 'h-[300px]';
                VarList.className = `bg-white border border-gray-400 shadow-inner w-full ${hauteurClass} overflow-y-auto scroll-list flex flex-col mb-3 text-black`;
                
                // Extraction de la liste des versions
                const listVersions = Object.values(col.list);
                let selectedVersionObj = listVersions[0]; 

                listVersions.forEach((verObj, index) => {
                    const item = document.createElement('div');
                    item.className = `py-2 text-center text-[2.5rem] leading-none font-bold underline decoration-[3px] underline-offset-[6px] cursor-pointer hover:bg-gray-100 transition-colors ${index === 0 ? 'item-selection' : ''}`;
                    item.textContent = verObj.version;
                    
                    item.addEventListener('click', () => {
                        PlaySound('click.mp3', basePath);
                        Array.from(VarList.children).forEach(enfant => enfant.classList.remove('item-selection'));
                        item.classList.add('item-selection');
                        selectedVersionObj = verObj;
                    });

                    VarList.appendChild(item);
                });

                // Bouton de Téléchargement
                const DownloadButton = document.createElement('button');
                DownloadButton.className = 'bg-[#00ff00] border-2 border-black w-full py-2.5 mb-1.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
                DownloadButton.textContent = 'Télécharger';
                DownloadButton.style.backgroundImage = `url('${basePath}sources/download.png')`;
                DownloadButton.style.backgroundRepeat = "repeat-x";
                DownloadButton.style.backgroundSize = "auto 100%";
                
                DownloadButton.addEventListener('click', () => {
                    PlaySound('click.mp3', basePath);
                    if (typeof afficherNotification === 'function') {
                        afficherNotification(`Téléchargement de la version ${selectedVersionObj.version} en cours...`);
                    }
                    const lien = document.createElement('a');
                    lien.href = basePath + selectedVersionObj.download;
                    document.body.appendChild(lien);
                    lien.click();
                    document.body.removeChild(lien);
                });

                // Bouton Changelog
                const ChangelogButton = document.createElement('button');
                ChangelogButton.className = 'bg-[#00ffff] border-2 border-black w-full py-2.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
                ChangelogButton.textContent = 'Changelog';
                ChangelogButton.style.backgroundImage = `url('${basePath}sources/changelog.png')`;
                ChangelogButton.style.backgroundRepeat = "repeat-x";
                ChangelogButton.style.backgroundSize = "auto 100%";
                
                ChangelogButton.addEventListener('click', () => {
                    PlaySound('click.mp3', basePath);
                    const modal = document.getElementById('changelog-modal');
                    const box = document.getElementById('changelog-box');
                    const titleZone = document.getElementById('changelog-title');
                    const contentZone = document.getElementById('changelog-content');

                    const cheminTxt = `${basePath}${col.folder}/changelogs/${selectedVersionObj.version}.txt`;

                    fetch(cheminTxt)
                        .then(response => {
                            if (!response.ok) throw new Error("Fichier introuvable");
                            return response.text();
                        })
                        .then(texte => {
                            titleZone.textContent = `Changelog - ${col.title} ${selectedVersionObj.version}`;
                            contentZone.textContent = texte;
                            
                            modal.classList.remove('opacity-0', 'pointer-events-none');
                            modal.classList.add('opacity-100', 'pointer-events-auto');
                            box.classList.remove('scale-95');
                            box.classList.add('scale-100');
                        })
                        .catch(() => {
                            if (typeof afficherNotification === 'function') {
                                afficherNotification(`⚠ Aucun changelog disponible pour la version ${selectedVersionObj.version}`);
                            }
                        });
                });

                divColonne.appendChild(Banner);
                divColonne.appendChild(VarList);
                divColonne.appendChild(DownloadButton);
                divColonne.appendChild(ChangelogButton);

                container.appendChild(divColonne);
            });
        })
        .catch(err => {
            console.error("Erreur lors de la lecture de packs.json :", err);
        });
}

// Fonction pour injecter dynamiquement les contenus spécifiques de la page (Titre, Description, Galerie)
function injecterContenuPack(packInfo, basePath) {
    const titleZone = document.getElementById('pack-title');
    const descriptionZone = document.getElementById('pack-description');
    const galleryZone = document.getElementById('pack-gallery');

    if (titleZone) {
        titleZone.textContent = packInfo.title;
    }

    if (descriptionZone) {
        // Remplacement de la variable $0 par le lien cliquable vers Vattic
        const vatticLink = '<a href="https://faithfulpack.net/" target="_blank" rel="noopener noreferrer" class="text-cyan-400 underline decoration-2 underline-offset-4 hover:text-cyan-300 transition-colors font-medium">Vattic</a>';
        let customHTML = packInfo.description.replace('$0', vatticLink);

        // Si un avertissement est spécifié, on l'ajoute en rouge en dessous
        if (packInfo.warning) {
            customHTML += `<br><span class="text-red-400 font-medium">${packInfo.warning}</span>`;
        }
        descriptionZone.innerHTML = customHTML;
    }

    if (galleryZone) {
        galleryZone.innerHTML = '';
        const screenImages = Object.values(packInfo.screen);
        
        const lightbox = document.getElementById('lightbox');
        const lightboxImg = document.getElementById('lightbox-img');

        screenImages.forEach(srcPath => {
            // Création de l'élément de la galerie
            const wrapper = document.createElement('div');
            wrapper.className = 'bg-black/40 border-2 border-black aspect-video flex items-center justify-center rounded shadow-md hover:border-white transition-colors duration-200 group relative overflow-hidden cursor-zoom-in';

            const img = document.createElement('img');
            img.src = basePath + srcPath;
            img.alt = "Illustration";
            img.className = 'w-full h-full object-cover gallery-trigger transition-transform duration-300 group-hover:scale-105';

            // Liaison de l'effet lightbox au clic sur l'image
            img.addEventListener('click', () => {
                if (typeof PlaySound === 'function') {
                    PlaySound('screen_in.mp3', basePath);
                }
                if (lightbox && lightboxImg) {
                    lightboxImg.src = img.src;
                    lightbox.classList.remove('hidden');
                    setTimeout(() => {
                        lightbox.classList.remove('opacity-0');
                        lightboxImg.classList.remove('scale-95');
                    }, 10);
                }
            });

            wrapper.appendChild(img);
            galleryZone.appendChild(wrapper);
        });

        // Configurer l'événement de fermeture de la lightbox une seule fois
        if (lightbox && !lightbox.dataset.init) {
            lightbox.dataset.init = "true";
            lightbox.addEventListener('click', () => {
                if (typeof PlaySound === 'function') {
                    PlaySound('screen_out.mp3', basePath);
                }
                lightbox.classList.add('opacity-0');
                lightboxImg.classList.add('scale-95');
                setTimeout(() => {
                    lightbox.classList.add('hidden');
                }, 300);
            });
        }
    }
}