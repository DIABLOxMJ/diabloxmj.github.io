if (typeof PlaySound !== 'function') {
    function PlaySound(nomFichier, basePath = '') {
        const audio = new Audio(`${basePath}sources/sound/${nomFichier}`);
        audio.volume = 0.25;
        audio.play().catch(() => {});
    }
}

function initialiserPageMods(containerMenuId, containerMainId, versionTextId, basePath = '') {
    const menuContainer = document.getElementById(containerMenuId);
    const mainContainer = document.getElementById(containerMainId);
    const versionTextZone = document.getElementById(versionTextId);

    if (!menuContainer || !mainContainer) return;

    // Étape 1 : Charger l'index central contenant les versions et les chemins de fichiers associés
    fetch(`${basePath}sources/data/modlist.json`)
        .then(response => {
            if (!response.ok) throw new Error("Impossible de charger modlist.json");
            return response.json();
        })
        .then(data => {
            if (data.length === 0) return;

            menuContainer.innerHTML = '';
            
            const listContainer = document.createElement('div');
            listContainer.className = 'bg-white border border-gray-400 shadow-inner w-full h-[700px] overflow-y-auto scroll-list flex flex-col mb-3 text-black';
            menuContainer.appendChild(listContainer);

            data.forEach((item, index) => {
                const versionBtn = document.createElement('div');
                versionBtn.className = `py-2 text-center text-[2.5rem] leading-none font-bold underline decoration-[3px] underline-offset-[6px] cursor-pointer hover:bg-gray-100 transition-colors ${index === 0 ? 'item-selection' : ''}`;
                versionBtn.textContent = item.id; // "id" contient la version à afficher (ex: "1.21.11")

                versionBtn.addEventListener('click', () => {
                    PlaySound('click.mp3', basePath);
                    
                    Array.from(listContainer.children).forEach(child => child.classList.remove('item-selection'));
                    versionBtn.classList.add('item-selection');

                    // Étape 2 : Charger dynamiquement le fichier JSON spécifique à cette version au clic
                    chargerEtAfficherLesMods(item.file, item.id, mainContainer, versionTextZone, basePath);
                });

                listContainer.appendChild(versionBtn);
            });

            // Charger dynamiquement la première version par défaut au chargement initial
            chargerEtAfficherLesMods(data[0].file, data[0].id, mainContainer, versionTextZone, basePath);
        })
        .catch(error => {
            console.error("Erreur lors de l'initialisation des versions :", error);
        });
}

// Nouvelle fonction intermédiaire asynchrone pour récupérer les données du fichier externe avant de l'afficher
function chargerEtAfficherLesMods(cheminFichier, versionNom, mainContainer, versionTextZone, basePath) {
    // Nettoyer le conteneur principal et afficher un message de chargement temporaire
    mainContainer.innerHTML = `<p class="text-xl italic text-gray-300 text-ombre p-4 text-center">Chargement des mods...</p>`;

    // On retire le premier slash éventuel du chemin pour construire correctement l'URL relative locale
    const cheminFormate = cheminFichier.startsWith('/') ? cheminFichier.substring(1) : cheminFichier;
    const urlReelle = `${basePath}sources/data/${cheminFormate}`;

    fetch(urlReelle)
        .then(response => {
            if (!response.ok) throw new Error(`Impossible de charger le fichier de liste : ${cheminFormate}`);
            return response.json();
        })
        .then(modsData => {
            // Le fichier de version peut contenir soit un objet associatif {}, soit un tableau [{}] selon ta structure
            // On extrait les éléments pour construire notre liste finale
            let modsObject = {};
            if (Array.isArray(modsData)) {
                // Si le fichier est un tableau d'objets (comme ton exemple), on les fusionne
                modsData.forEach(element => {
                    modsObject = { ...modsObject, ...element };
                });
            } else {
                modsObject = modsData;
            }

            afficherLesMods(modsObject, versionNom, mainContainer, versionTextZone, basePath);
        })
        .catch(error => {
            console.error("Erreur lors du chargement de la liste de mods :", error);
            mainContainer.innerHTML = `<p class="text-xl italic text-red-400 text-ombre p-4 text-center">⚠ Impossible de charger la liste de mods pour cette version.</p>`;
        });
}

function afficherLesMods(modsObject, versionNom, mainContainer, versionTextZone, basePath) {
    if (versionTextZone) {
        versionTextZone.textContent = versionNom;
    }

    mainContainer.innerHTML = '';

    const listeMods = Object.values(modsObject);

    if (listeMods.length === 0) {
        mainContainer.innerHTML = `<p class="text-xl italic text-gray-300 text-ombre p-4 text-center">Aucun mod enregistré pour cette version.</p>`;
        return;
    }

    listeMods.forEach(mod => {
        const card = document.createElement('div');
        card.className = 'bg-[#151515]/50 border-2 border-neutral-700/60 p-4 flex flex-col sm:flex-row items-center sm:items-start gap-4 shadow-md text-white text-ombre';

        const img = document.createElement('img');
        img.src = mod.img;
        img.alt = mod.title;
        img.className = 'w-24 h-24 bg-gray-900 border border-neutral-700 object-cover shrink-0 rounded';

        const infosDiv = document.createElement('div');
        infosDiv.className = 'flex-grow flex flex-col space-y-1 text-left w-full';

        const title = document.createElement('h3');
        title.className = 'text-2xl font-bold leading-tight';

        // Si un lien ou un site officiel est présent dans le JSON pour ce mod
        if (mod.link) {
            const titleLink = document.createElement('a');
            titleLink.href = mod.link;
            titleLink.target = '_blank';
            titleLink.className = 'hover:underline hover:text-cyan-400 transition-colors cursor-pointer';
            titleLink.textContent = mod.title;
            title.appendChild(titleLink);
        } else {
            title.textContent = mod.title;
        }

        const description = document.createElement('p');
        description.className = 'text-base font-medium leading-snug text-neutral-200';
        description.textContent = mod.description;

        const depParagraph = document.createElement('p');
        depParagraph.className = 'text-sm font-semibold text-neutral-400 pt-1';
        
        const tabDependances = Object.values(mod.Dependant || {});
        if (tabDependances.length > 0) {
            depParagraph.textContent = "Dépendance : ";
            tabDependances.forEach((dep, idx) => {
                const depLink = document.createElement('a');
                depLink.href = dep.link;
                depLink.target = "_blank";
                depLink.className = 'text-cyan-400 hover:text-cyan-300 underline underline-offset-2 hover:font-bold transition-all';
                depLink.textContent = dep.Title;

                depParagraph.appendChild(depLink);

                if (idx < tabDependances.length - 1) {
                    const separator = document.createTextNode(idx === tabDependances.length - 2 ? ' et ' : ', ');
                    depParagraph.appendChild(separator);
                }
            });
        } else {
            depParagraph.textContent = "Dépendance : Aucune";
        }

        infosDiv.appendChild(title);
        infosDiv.appendChild(description);
        infosDiv.appendChild(depParagraph);

        const actionContainer = document.createElement('div');
        actionContainer.className = 'shrink-0 w-full sm:w-auto flex flex-col items-center justify-start pt-2 sm:pt-4 space-y-2';

        const downloadBtn = document.createElement('a');
        downloadBtn.href = mod.download;
        downloadBtn.target = "_blank";
        downloadBtn.className = 'bg-[#00ff00] border-2 border-black w-full sm:w-auto px-6 py-2 font-bold text-white text-center text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm block text-xl';
        downloadBtn.textContent = 'Télécharger';
        
        downloadBtn.style.backgroundImage = `url('${basePath}sources/img/download.png')`;
        downloadBtn.style.backgroundRepeat = "repeat-x";
        downloadBtn.style.backgroundSize = "auto 100%";
        
        downloadBtn.addEventListener('click', () => {
            PlaySound('click.mp3', basePath);
        });

        actionContainer.appendChild(downloadBtn);

        // Intégration du texte de la version du mod sous le bouton télécharger
        if (mod.mod_version) {
            const modVerText = document.createElement('span');
            modVerText.className = 'text-m text-neutral-400 font-bold';
            modVerText.textContent = `Version : ${mod.mod_version}`;
            actionContainer.appendChild(modVerText);
        }

        card.appendChild(img);
        card.appendChild(infosDiv);
        card.appendChild(actionContainer);

        mainContainer.appendChild(card);
    });
}