let currentTab = localStorage.getItem('mcp_currentTab') || 'blocks'; 
		let lastInspectionTab = localStorage.getItem('mcp_lastInspectionTab') || 'blocks';
		let currentSelectedId = localStorage.getItem('mcp_currentSelectedId') || null;
		let filterOnlyDiff = JSON.parse(localStorage.getItem('mcp_filterOnlyDiff')) || false;
		let filterOnlyMissing = JSON.parse(localStorage.getItem('mcp_filterOnlyMissing')) || false;
		
		let imageSize = parseInt(localStorage.getItem('mcp_imageSize')) || 256;
		let filterComparatorFolder = localStorage.getItem('mcp_filterComparatorFolder') || null;
		let savedSearchQuery = localStorage.getItem('mcp_searchQuery') || '';
		const isHidden = localStorage.getItem('mcp_panneauQuery') === 'true';
		const panneau = document.getElementById('cadre-outils');
		if (isHidden) {
			panneau.classList.add('hidden');
		}

		let dbBlocks = [];
		let dbItems = [];
		let dbCategories = {};
		let dbComparaison = [];
		let currentFilteredElements = [];

		let completedTasks = JSON.parse(localStorage.getItem('mcp_completedTasks')) || [];
		let selectedForExtraction = new Set();

		const dossiersRacinesMinecraft = [
			"blockstates", "font", "lang", "models", "particles", "sounds", "texts", "textures"
		];

		window.onload = function() {
			applyImageSize(imageSize);

			const searchInput = document.getElementById('search');
			if (searchInput) searchInput.value = savedSearchQuery;
			
			const btnFilterDiff = document.getElementById('btn-filter-diff');
			if(filterOnlyDiff && btnFilterDiff) btnFilterDiff.classList.add('active');

			const btnFilter = document.getElementById('btn-filter-missing');
			if(filterOnlyMissing && btnFilter) btnFilter.classList.add('active');

			Promise.all([
				fetch(`http://localhost:5000/get-index/blocks`).then(r => r.ok ? r.json() : []),
				fetch(`http://localhost:5000/get-index/items`).then(r => r.ok ? r.json() : []),
				fetch(`http://localhost:5000/get-categories`).then(r => r.ok ? r.json() : {}),
				fetch(`http://localhost:5000/get-Comparaison`).then(r => r.ok ? r.json() : [])
			]).then(([blocks, items, categories, Comparaison]) => {
				dbBlocks = blocks;
				dbItems = items;
				dbCategories = categories;
				dbComparaison = Comparaison;

				genererBoutonsCategories();

				if (currentTab === 'blocks' || currentTab === 'items' || currentTab === 'comparaison') {
					switchTab(currentTab, false);
				} else {
					switchToCategory(currentTab, false);
				}

				if (currentSelectedId) {
					restaurerDernierElement();
				}

				updateToolbarVisibility();
			});
		};

		function togglePanneauElements() {
			const panneau = document.getElementById('cadre-outils');
			panneau.classList.toggle('hidden');
			const panneauHidden = panneau.classList.contains('hidden');
			localStorage.setItem('mcp_panneauQuery', panneauHidden);
		}

		function toggleZoom() {
			imageSize = (imageSize === 256) ? 128 : 256;
			localStorage.setItem('mcp_imageSize', imageSize);
			applyImageSize(imageSize);
		}

		function applyImageSize(size) {
			document.documentElement.style.setProperty('--img-size', `${size}px`);
			const btn = document.getElementById('btn-zoom');
			if (btn) btn.innerText = `🔍 Zoom: ${size}px`;
		}

		function onSearchInput() {
			const query = document.getElementById('search').value;
			localStorage.setItem('mcp_searchQuery', query);
			filtrerElements();
		}

		function getAntiCacheUrl(url) {
			if (!url) return url;
			const sep = url.includes('?') ? '&' : '?';
			return `${url}${sep}_v=${Date.now()}`;
		}

		function formatCodeWithLineNumbers(rawText) {
			let formattedText = rawText;
			try {
				const parsed = JSON.parse(rawText);
				formattedText = JSON.stringify(parsed, null, 4);
			} catch(e) {}

			const lines = formattedText.split('\n');
			const totalLines = lines.length;
			const padSize = totalLines.toString().length;

			return lines.map((line, index) => {
				const lineNum = (index + 1).toString().padStart(padSize, ' ');
				return `<span class="line-number">${lineNum}</span>${escapeHtml(line)}`;
			}).join('\n');
		}

		function escapeHtml(text) {
			return text
				.replace(/&/g, "&amp;")
				.replace(/</g, "&lt;")
				.replace(/>/g, "&gt;")
				.replace(/"/g, "&quot;")
				.replace(/'/g, "&#039;");
		}

		function toggleOutilsIndex() {
			const container = document.getElementById('conteneur-outils-scripts');
			const btn = document.getElementById('btn-toggle-outils');
			if (container.style.display === 'none') {
				container.style.display = 'block';
				btn.innerText = 'Masquer';
			} else {
				container.style.display = 'none';
				btn.innerText = 'Afficher';
			}
		}

		function toggleFilterDiff() {
			filterOnlyDiff = !filterOnlyDiff;
			localStorage.setItem('mcp_filterOnlyDiff', filterOnlyDiff);

			const btnFilter = document.getElementById('btn-filter-diff');
			if (btnFilter) btnFilter.classList.toggle('active', filterOnlyDiff);

			filtrerElements();
		}

		function toggleFilterMissing() {
			filterOnlyMissing = !filterOnlyMissing;
			localStorage.setItem('mcp_filterOnlyMissing', filterOnlyMissing);
			
			const btnFilter = document.getElementById('btn-filter-missing');
			if (btnFilter) btnFilter.classList.toggle('active', filterOnlyMissing);
			
			filtrerElements();
		}

		function genererBoutonsCategories() {
			const container = document.getElementById('liste-categories-scroll');
			if (!container) return;
			container.innerHTML = '';

			if (currentTab === 'comparaison') {
				const btnTous = document.createElement('button');
				btnTous.className = 'btn-outil';
				btnTous.style.borderLeft = filterComparatorFolder === null ? '4px solid #ff9800' : '4px solid #555';
				btnTous.style.marginBottom = '6px';
				btnTous.style.width = '100%';
				btnTous.innerText = `📂 TOUT AFFICHER`;
				btnTous.onclick = () => {
					filterComparatorFolder = null;
					localStorage.removeItem('mcp_filterComparatorFolder');
					genererBoutonsCategories();
					filtrerElements();
				};
				container.appendChild(btnTous);

				let findFolders = new Set();
				dbComparaison.forEach(e => {
					if (e.path) {
						const parts = e.path.split('/');
						if (parts.length > 1) {
							let ropeRoad = "";
							for (let i = 0; i < parts.length - 1; i++) {
								ropeRoad += (ropeRoad ? "/" : "") + parts[i];
								findFolders.add(ropeRoad);
							}
						}
					}
				});

				const listeDossiersTries = Array.from(findFolders).sort();
				let closeFolders = JSON.parse(localStorage.getItem('mcp_closeFolders')) || [];

				listeDossiersTries.forEach(cheminDossier => {
					const niveau = cheminDossier.split('/').length - 1; 
					const nomDossier = cheminDossier.split('/').pop();   
					const aDesEnfants = listeDossiersTries.some(d => d.startsWith(cheminDossier + '/') && d.split('/').length === cheminDossier.split('/').length + 1);
				
					const estMasqueParUnAncetre = closeFolders.some(dossierFerme => {
						return cheminDossier.startsWith(dossierFerme + '/');
					});

					if (estMasqueParUnAncetre) return;

					const btn = document.createElement('button');
					btn.className = 'btn-outil';
					btn.style.width = '100%';
					btn.style.display = 'flex';
					btn.style.justifyContent = 'space-between';
					btn.style.alignItems = 'center';
				
					const spanTexte = document.createElement('span');
					spanTexte.style.overflow = 'hidden';
					spanTexte.style.textOverflow = 'ellipsis';
				
					if (niveau === 0) {
						btn.classList.add('btn-dossier-racine');
						spanTexte.innerText = `📁 ${nomDossier}`;
					} else {
						btn.classList.add('btn-sous-dossier');
						btn.style.paddingLeft = `${(niveau * 16) + 8}px`;
						spanTexte.innerText = `└ 📂 ${nomDossier}`;
					}
					btn.appendChild(spanTexte);
				
					if (aDesEnfants) {
						const isClose = closeFolders.includes(cheminDossier);
						const btnToggle = document.createElement('span');
						btnToggle.innerText = isClose ? ' ( ▲ Masquer ) ' : ' ( ▼ Afficher ) ';
						btnToggle.style.padding = '0 4px';
						btnToggle.style.cursor = 'pointer';
						btnToggle.style.fontSize = '0.75rem';

						btnToggle.onclick = (e) => {
							e.stopPropagation();
							if (isClose) {
								closeFolders = closeFolders.filter(d => d !== cheminDossier);
							} else {
								closeFolders.push(cheminDossier);
							}
							localStorage.setItem('mcp_closeFolders', JSON.stringify(closeFolders));
							genererBoutonsCategories();
						};
						btn.appendChild(btnToggle);
					}
				
					if (filterComparatorFolder === cheminDossier) {
						btn.style.backgroundColor = 'var(--bg-bouton-hover)';
						btn.style.fontWeight = 'bold';
					}
				
					btn.onclick = () => {
						filterComparatorFolder = cheminDossier;
						localStorage.setItem('mcp_filterComparatorFolder', filterComparatorFolder);
						genererBoutonsCategories(); 
						filtrerElements();	  
					};
					container.appendChild(btn);
				});
			} else {
				Object.keys(dbCategories).forEach(catName => {
					const btn = document.createElement('button');
					btn.className = 'btn-outil btn-cat-dynamique';
					btn.style.borderLeft = '4px solid #64b5f6';
					btn.style.width = '100%';
					btn.innerText = `📂 ${catName.toUpperCase()}`;
					btn.onclick = () => switchToCategory(catName);
					container.appendChild(btn);
				});
			}
		}

		function genererListe(elements) {
			const container = document.getElementById('liste-elements');
			container.innerHTML = '';

			let filteredElements = elements;

			if (filterOnlyMissing) {
				if (currentTab === 'comparaison') {
					filteredElements = filteredElements.filter(e => e.status && e.status.toLowerCase() === 'missing');
				} else {
					filteredElements = filteredElements.filter(e => !completedTasks.includes(e.id));
				}
			}
			
			if (filterOnlyDiff && currentTab === 'comparaison') {
				filteredElements = filteredElements.filter(e => e.has_content_diff === true);
			}
		
			const counterSpan = document.getElementById('counter-elements-visible');
			if (counterSpan) {
				counterSpan.innerText = `${filteredElements.length} élément(s)`;
			}
		
			if (!filteredElements || filteredElements.length === 0) {
				container.innerHTML = `<p style="color: #888; text-align: center; margin-top: 20px;">Aucun élément (ou tout est filtré/complété ! 🎉)</p>`;
				updateExtractionButtonVisual();
				return;
			}
		
			filteredElements.forEach(elem => {
				const wrapper = document.createElement('div');
				wrapper.style.display = 'flex';
				wrapper.style.alignItems = 'center';
				wrapper.style.gap = '6px';
				wrapper.style.marginBottom = '4px';
			
				const elemIdentifier = currentTab === 'comparaison' ? elem.path : elem.id;
			
				if (currentTab === 'comparaison') {
					const chk = document.createElement('input');
					chk.type = 'checkbox';
					chk.style.cursor = 'pointer';
					chk.style.width = '16px';
					chk.style.height = '16px';
					chk.checked = selectedForExtraction.has(elemIdentifier);
				
					chk.onchange = (e) => {
						e.stopPropagation();
						if (chk.checked) {
							selectedForExtraction.add(elemIdentifier);
						} else {
							selectedForExtraction.delete(elemIdentifier);
						}
						updateExtractionButtonVisual();
					};
					wrapper.appendChild(chk);
				}
			
				const btn = document.createElement('button');
				btn.className = 'btn-catalogue';
				btn.style.flex = '1';
				btn.style.marginBottom = '0';
			
				const isSelected = (currentSelectedId === elemIdentifier);
				if (isSelected) {
					btn.classList.add('comp-selected');
				}
			
				if (currentTab === 'comparaison') {
					const diffEmoji = elem.has_content_diff ? ' ⚠️' : '';
					const txtSuffix = isSelected ? '  👈 ( Ouvert )' : '';
					btn.innerText = elem.path + diffEmoji + txtSuffix;
					btn.style.fontSize = "0.8rem";
					if (elem.status === 'missing') {
						btn.classList.add('comp-missing');
					} else {
						btn.classList.add('comp-present');
					}
				} else {
					const txtSuffix = isSelected ? '  👈 ( Ouvert )' : '';
					btn.innerText = elem.buttonName + txtSuffix;
					if (completedTasks.includes(elem.id)) {
						btn.classList.add('valide');
					}
				}
			
				btn.onclick = () => {
					currentSelectedId = elemIdentifier;
					localStorage.setItem('mcp_currentSelectedId', currentSelectedId);
				
					genererListe(filteredElements);
				
					if (currentTab === 'blocks' || currentTab === 'items') {
						afficherDetails(elem);
					} else if (currentTab === 'comparaison') {
						afficherDetailsComparaison(elem);
					} else {
						afficherDetailsCategorie(elem);
					}
					updateTaskButtonVisual();
				};
			
				wrapper.appendChild(btn);
				container.appendChild(wrapper);
			});
		
			updateExtractionButtonVisual();
		}

		function toggleSelectAll(select) {
			currentFilteredElements.forEach(elem => {
				const id = currentTab === 'comparaison' ? elem.path : elem.id;
			
				if (filterOnlyMissing) {
					if (currentTab === 'comparaison' && elem.status && elem.status.toLowerCase() !== 'missing') return;
					if (currentTab !== 'comparaison' && completedTasks.includes(elem.id)) return;
				}
			
				if (filterOnlyDiff && currentTab === 'comparaison') {
					if (!elem.has_content_diff) return;
				}
			
				if (select) {
					selectedForExtraction.add(id);
				} else {
					selectedForExtraction.delete(id);
				}
			});
		
			genererListe(currentFilteredElements);
		}

		function updateExtractionButtonVisual() {
			const btn = document.getElementById('extract-single-btn');
			if (!btn) return;
		
			const count = selectedForExtraction.size;
			if (count > 0) {
				btn.innerText = `📦 Extraire la sélection (${count})`;
				btn.style.backgroundColor = '#d81b60';
			} else {
				btn.innerText = "📦 Extraire le fichier";
				btn.style.backgroundColor = '#e91e63';
			}
		}
		
		function gererActionExtraction() {
			if (selectedForExtraction.size > 0) {
				extraireFichiersCoches();
			} else {
				extraireFichierSelectionne();
			}
		}

		function extraireFichiersCoches() {
			if (selectedForExtraction.size === 0) return;
		
			const listPaths = Array.from(selectedForExtraction);
			const btn = document.getElementById('extract-single-btn');
			const oldText = btn.innerText;
		
			btn.innerText = "⏳ Extraction en cours...";
			btn.disabled = true;
		
			fetch('http://localhost:5000/extract-batch-files', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ paths: listPaths })
			})
			.then(r => r.json())
			.then(data => {
				if (data.status === 'success') {
					alert(`🎉 ${data.extracted_count} fichier(s) extrait(s) avec succès dans le dossier Extract !`);
					selectedForExtraction.clear();
					filtrerElements();
				} else {
					alert("❌ Erreur : " + data.message);
				}
			})
			.catch(err => {
				alert("❌ Impossible de contacter le serveur.");
			})
			.finally(() => {
				btn.innerText = oldText;
				btn.disabled = false;
			});
		}

		function updateToolbarVisibility() {
			const toolbar = document.getElementById('toolbar-actions');
			if (toolbar) {
				toolbar.style.display = (currentTab === 'comparaison') ? 'flex' : 'none';
			}
		}

		function switchToInspectionMode() {
			switchTab(lastInspectionTab, true);
		}

		function switchTab(type, resetSelection = true) {
			currentTab = type;
			localStorage.setItem('mcp_currentTab', type);
			if (type === 'blocks' || type === 'items') {
				lastInspectionTab = type;
				localStorage.setItem('mcp_lastInspectionTab', type);
			}

			if (resetSelection) resetSelectedElement();

			if (type !== 'comparaison') {
				filterComparatorFolder = null;
				localStorage.removeItem('mcp_filterComparatorFolder');
			}

			const subMenu = document.getElementById('sub-menu-inspection');
			const tabInspection = document.getElementById('tab-inspection');
			const tabComparaison = document.getElementById('tab-comparaison');
			const btnDiff = document.getElementById('btn-filter-diff');
			const btnMissing = document.getElementById('btn-filter-missing');

			if (type === 'blocks' || type === 'items') {
				subMenu.style.display = 'flex';
				tabInspection.classList.add('actif');
				tabComparaison.classList.remove('actif');
				btnDiff.style.display = 'none';
				btnMissing.style.display = 'none';
			} else if (type === 'comparaison') {
				subMenu.style.display = 'none';
				tabInspection.classList.remove('actif');
				tabComparaison.classList.add('actif');
				btnDiff.style.display = 'flex';
				btnMissing.style.display = 'flex';
			} else {
				subMenu.style.display = 'none';
				tabInspection.classList.remove('actif');
				tabComparaison.classList.remove('actif');
				btnDiff.style.display = 'none';
				btnMissing.style.display = 'none';
			}

			document.getElementById('tab-blocks').classList.toggle('actif', type === 'blocks');
			document.getElementById('tab-items').classList.toggle('actif', type === 'items');

			updateToolbarVisibility();
			genererBoutonsCategories();
			filtrerElements();
		}

		function switchToCategory(catName, resetSelection = true) {
			currentTab = catName;
			localStorage.setItem('mcp_currentTab', catName);
			if(resetSelection) resetSelectedElement();
			filterComparatorFolder = null;
			localStorage.removeItem('mcp_filterComparatorFolder');

			document.getElementById('sub-menu-inspection').style.display = 'none';
			document.getElementById('tab-inspection').classList.remove('actif');
			document.getElementById('tab-blocks').classList.remove('actif');
			document.getElementById('tab-items').classList.remove('actif');
			document.getElementById('tab-comparaison').classList.remove('actif');

			document.getElementById('btn-filter-diff').style.display = 'none';
			document.getElementById('btn-filter-missing').style.display = 'none';

			updateToolbarVisibility();
			genererBoutonsCategories();
			genererListe(dbCategories[catName]);
		}

		function resetSelectedElement() {
			currentSelectedId = null;
			localStorage.removeItem('mcp_currentSelectedId');
			document.getElementById('zone-aperçu-vide').style.display = 'block';
			document.getElementById('zone-aperçu-contenu').style.display = 'none';
		}

		function restaurerDernierElement() {
			let listToSearch = [];
			if (currentTab === 'blocks') listToSearch = dbBlocks;
			else if (currentTab === 'items') listToSearch = dbItems;
			else if (currentTab === 'comparaison') listToSearch = dbComparaison;
			else listToSearch = dbCategories[currentTab] || [];

			const target = listToSearch.find(e => (currentTab === 'comparaison' ? e.path : e.id) === currentSelectedId);
			if (target) {
				if (currentTab === 'blocks' || currentTab === 'items') afficherDetails(target);
				else if (currentTab === 'comparaison') afficherDetailsComparaison(target);
				else afficherDetailsCategorie(target);
				updateTaskButtonVisual();
			}
		}

		function filtrerElements() {
			const rawQuery = (document.getElementById('search').value || '').trim();
			let baseData = [];

			if (currentTab === 'blocks') baseData = dbBlocks;
			else if (currentTab === 'items') baseData = dbItems;
			else if (currentTab === 'comparaison') baseData = dbComparaison;
			else baseData = dbCategories[currentTab] || [];

			if (currentTab === 'comparaison' && filterComparatorFolder) {
				baseData = baseData.filter(e => {
					if (!e.path) return false;
					const pathLoweCase = e.path.toLowerCase();
					const filtreLowerCase = filterComparatorFolder.toLowerCase();
					return pathLoweCase.startsWith(filtreLowerCase + '/');
				});
			}
		
			const tokens = rawQuery.toLowerCase().split(/\s+/).filter(t => t.length > 0);
			const includes = [];
			const excludes = [];
		
			tokens.forEach(token => {
				if (token.startsWith('-') && token.length > 1) {
					excludes.push(token.substring(1));
				} else {
					includes.push(token);
				}
			});
		
			const filtres = baseData.filter(elem => {
				let textToSearch = "";
				if (currentTab === 'comparaison') {
					textToSearch = (elem.path || "").toLowerCase();
				} else {
					textToSearch = ((elem.buttonName || "") + " " + (elem.id || "")).toLowerCase();
				}
			
				const isExcluded = excludes.some(ex => textToSearch.includes(ex));
				if (isExcluded) return false;
			
				if (includes.length === 0) return true;
			
				return includes.every(inc => {
					if (currentTab === 'comparaison' && (inc === '⚠️' || inc === 'diff')) {
						return elem.has_content_diff === true;
					}
					return textToSearch.includes(inc);
				});
			});
		
			currentFilteredElements = filtres;
			genererListe(filtres);
		}

		function chargerFichierDansVisualiseur(path) {
			const codeBox = document.getElementById('code-box');
			const sectionCode = document.getElementById('section-code-viewer');
			
			document.getElementById('section-code-viewer-diff').style.display = 'none';
			sectionCode.style.display = 'block';
			codeBox.innerText = "⏳ Lecture du contenu par le serveur...";

			fetch(`http://localhost:5000/read-file`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ path: path })
			})
			.then(response => response.json())
			.then(data => {
				if (data.status === 'success') {
					codeBox.innerHTML = formatCodeWithLineNumbers(data.content);
				} else {
					codeBox.innerText = `Erreur : Impossible de lire le fichier.\nChemin : ${path}`;
				}
			})
			.catch(err => {
				codeBox.innerText = `Erreur de connexion au serveur local.\nChemin cible : ${path}`;
			});
		}

		function chargerFichiersComparaisonDiff(mainPath, nextPath, defaultPath) {
			const sectionCodeDiff = document.getElementById('section-code-viewer-diff');
			const sectionCodeSingle = document.getElementById('section-code-viewer');
			const boxMain = document.getElementById('code-box-main');
			const boxNext = document.getElementById('code-box-next');

			const headerMain = document.getElementById('header-main-file');
			const headerNext = document.getElementById('header-next-file');

			headerMain.onclick = () => systemOpen(mainPath, 'folder');
			headerNext.onclick = () => systemOpen(nextPath, 'folder');

			sectionCodeSingle.style.display = 'none';
			sectionCodeDiff.style.display = 'block';

			boxMain.innerText = "⏳ Lecture du fichier Main...";
			boxNext.innerText = "⏳ Lecture du fichier Next...";

			fetch(`http://localhost:5000/read-file`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ path: mainPath })
			})
			.then(r => r.json())
			.then(data => {
				if (data.status === 'success') {
					boxMain.innerHTML = formatCodeWithLineNumbers(data.content);
				} else {
					boxMain.innerText = `⚠️ Fichier introuvable ou absent dans Pack (Main).\nChemin : ${mainPath}`;
				}
			})
			.catch(err => { boxMain.innerText = "Erreur réseau au chargement de Pack (Main)"; });
		
			fetch(`http://localhost:5000/read-file`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ path: nextPath })
			})
			.then(r => r.json())
			.then(data => {
				if (data.status === 'success') {
					headerNext.innerText = "📁 Pack (Next) - Nouveau ↗️";
					boxNext.innerHTML = formatCodeWithLineNumbers(data.content);
				} else {
					console.warn(`[Next] non trouvé (${nextPath}). Tentative sur [Default]...`);
					return fetch(`http://localhost:5000/read-file`, {
						method: 'POST',
						headers: { 'Content-Type': 'application/json' },
						body: JSON.stringify({ path: defaultPath })
					})
					.then(r => r.json())
					.then(defaultData => {
						if (defaultData.status === 'success') {
							headerNext.innerText = "📁 Pack (Default) - Par défaut ↗️";
							headerNext.onclick = () => systemOpen(defaultPath, 'folder');
							boxNext.innerHTML = formatCodeWithLineNumbers(defaultData.content);
						} else {
							boxNext.innerText = `⚠️ Fichier introuvable dans Pack (Next) ni dans Pack (Default).\nNext : ${nextPath}\nDefault : ${defaultPath}`;
						}
					});
				}
			})
			.catch(err => { 
				boxNext.innerText = "Erreur réseau au chargement du fichier de comparaison."; 
			});
		}

		function afficherDetails(elem) {
			document.getElementById('zone-aperçu-vide').style.display = 'none';
			document.getElementById('zone-aperçu-contenu').style.display = 'block';
			document.getElementById('section-json-fichiers').style.display = 'block';
			document.getElementById('section-comparaison-details').style.display = 'none';
			document.getElementById('section-textures').style.display = 'block';
			document.getElementById('task-btn').style.display = 'block';
			document.getElementById('section-code-viewer').style.display = 'none';
			document.getElementById('section-code-viewer-diff').style.display = 'none';

			document.getElementById('preview-title').innerText = elem.buttonName;
			document.getElementById('preview-id').innerText = elem.id;

			const blockstateRow = document.getElementById('row-blockstate');
			const bStateSpan = document.getElementById('path-blockstate');
			if (currentTab === 'blocks' && elem.blockstate) {
				blockstateRow.style.display = 'block';
				bStateSpan.innerText = elem.blockstate;
				bStateSpan.className = "clickable-path";
				bStateSpan.onclick = () => {
					// systemOpen(elem.blockstate, 'file');
					chargerFichierDansVisualiseur(elem.blockstate);
				};
			} else {
				blockstateRow.style.display = 'none';
				bStateSpan.onclick = null;
			}
		
			const itemDefRow = document.getElementById('row-item-def');
			const itemDefSpan = document.getElementById('path-item-def');
			if (currentTab === 'items' && elem.item_definition) {
				itemDefRow.style.display = 'block';
				itemDefSpan.innerText = elem.item_definition;
				itemDefSpan.className = "clickable-path";
				itemDefSpan.onclick = () => {
					// systemOpen(elem.item_definition, 'file');
					chargerFichierDansVisualiseur(elem.item_definition);
				};
			} else {
				itemDefRow.style.display = 'none';
				itemDefSpan.onclick = null;
			}
		
			const modelSpan = document.getElementById('path-model');
			modelSpan.innerHTML = '';
		
			let modelsList = elem.models || (elem.model ? [elem.model] : []);
		
			if (modelsList.length > 0) {
				modelsList.forEach((mPath, idx) => {
					if (mPath.includes("None.json")) return;
				
					const pLink = document.createElement('span');
					pLink.className = "clickable-path";
					pLink.style.display = "block";
					pLink.style.marginTop = idx > 0 ? "2px" : "0px";
					pLink.innerText = (modelsList.length > 1 ? `• ` : '') + mPath;
					pLink.onclick = () => {
						// systemOpen(mPath, 'file');
						chargerFichierDansVisualiseur(mPath);
					};
					modelSpan.appendChild(pLink);
				});
			}
		
			if (!modelSpan.hasChildNodes()) {
				modelSpan.innerText = "Non défini (Généré par le code du jeu)";
				modelSpan.className = "";
				modelSpan.onclick = null;
			}
		
			const grid = document.getElementById('preview-textures-grid');
			grid.classList.remove('mode-texte');
			grid.innerHTML = '';
		
			if(!elem.textures || Object.keys(elem.textures).length === 0) {
				grid.innerHTML = '<p style="color: #aaa; font-style: italic;">Aucune texture détectée.</p>';
				return;
			}
		
			Object.keys(elem.textures).forEach(key => {
				const cheminTexture = elem.textures[key];
				const card = document.createElement('div');
				card.className = 'card-texture';
				card.onclick = () => systemOpen(cheminTexture, 'file');

				const resId = `res-${key}-${elem.id}`.replace(/:/g, '-').replace(/\//g, '-');
				card.innerHTML = `
					<img src="${getAntiCacheUrl(cheminTexture)}" onerror="handleImgError(this)" onload="afficherResolution(this, '${resId}')">
					<strong>${key}</strong><br>
					<span>${cheminTexture.split('/').pop()}</span><br>
					<span id="${resId}" style="color: #4caf50; font-weight: bold; font-size: 0.75rem; display:block; margin-top:4px;">🔍 Chargement...</span>
				`;
				grid.appendChild(card);
			});
		}

		function afficherDetailsCategorie(elem) {
			document.getElementById('zone-aperçu-vide').style.display = 'none';
			document.getElementById('zone-aperçu-contenu').style.display = 'block';
			document.getElementById('section-json-fichiers').style.display = 'none';
			document.getElementById('section-comparaison-details').style.display = 'none';
			document.getElementById('section-textures').style.display = 'block';
			document.getElementById('task-btn').style.display = 'block';
			document.getElementById('section-code-viewer').style.display = 'none';
			document.getElementById('section-code-viewer-diff').style.display = 'none';
			
			document.getElementById('preview-title').innerText = elem.buttonName;
			document.getElementById('preview-id').innerText = `Dossier : ${elem.folder_path}`;

			const grid = document.getElementById('preview-textures-grid');
			grid.classList.remove('mode-texte');
			grid.innerHTML = '';
			
			elem.textures.forEach(tex => {
				const card = document.createElement('div');
				card.className = 'card-texture';
				card.onclick = () => systemOpen(tex.path, 'file');
				
				const resId = `res-cat-${elem.id}-${tex.name}`.replace(/[^a-zA-Z0-9]/g, '-');
				card.innerHTML = `
					<img src="${getAntiCacheUrl(tex.path)}" onerror="handleImgError(this)" onload="afficherResolution(this, '${resId}')">
					<strong>${tex.name.replace(/_/g, ' ')}</strong><br>
					<span>${tex.filename}</span><br>
					<span id="${resId}" style="color: #4caf50; font-weight: bold; font-size: 0.75rem; display:block; margin-top:4px;">🔍 Chargement...</span>
				`;
				grid.appendChild(card);
			});
		}

		function afficherDetailsComparaison(elem) {
			document.getElementById('zone-aperçu-vide').style.display = 'none';
			document.getElementById('zone-aperçu-contenu').style.display = 'block';
			document.getElementById('section-json-fichiers').style.display = 'none';
			document.getElementById('section-comparaison-details').style.display = 'block';
			document.getElementById('task-btn').style.display = 'none';
			document.getElementById('section-code-viewer').style.display = 'none';
			document.getElementById('section-code-viewer-diff').style.display = 'none';

			document.getElementById('preview-title').innerText = elem.filename;
			document.getElementById('preview-id').innerText = `Catégorie : ${elem.category.toUpperCase()}`;

			const badge = document.getElementById('comp-status-badge');
			if (elem.status === 'missing') {
				badge.innerText = "❌ ABSENT";
				badge.style.color = "var(--rouge-manquant)";
			} else {
				badge.innerText = "✔️ PRÉSENT";
				badge.style.color = "var(--accent)";
			}

			const grid = document.getElementById('preview-textures-grid');
			grid.classList.remove('mode-texte');
			grid.innerHTML = '';
			
			const mainPath = `Pack (Main)/assets/${elem.path}`;
			const nextPath = `Pack (Next)/assets/${elem.path}`;
			const defaultPath = `Pack (Default)/assets/${elem.path}`;

			if (elem.filename.endsWith('.png')) {
				document.getElementById('section-textures').style.display = 'block';
				
				const cardMain = document.createElement('div');
				cardMain.className = 'card-texture';
				cardMain.onclick = () => systemOpen(mainPath, 'file');
				const resIdMain = `res-main-${elem.filename}`.replace(/[^a-zA-Z0-9]/g, '-');
				cardMain.innerHTML = `
					<img src="${getAntiCacheUrl(mainPath)}" onerror="handleImgError(this)" onload="afficherResolution(this, '${resIdMain}')">
					<strong>Pack (Main)</strong><br>
					<span>${elem.filename}</span><br>
					<span id="${resIdMain}" style="color: #4caf50; font-weight: bold; font-size: 0.75rem; display:block; margin-top:4px;">🔍 Chargement...</span>
				`;
				grid.appendChild(cardMain);

				const cardNext = document.createElement('div');
				cardNext.className = 'card-texture';
				cardNext.onclick = () => systemOpen(nextPath, 'file');
				const resIdNext = `res-next-${elem.filename}`.replace(/[^a-zA-Z0-9]/g, '-');
				cardNext.innerHTML = `
					<img src="${getAntiCacheUrl(nextPath)}" onerror="handleImgError(this)" onload="afficherResolution(this, '${resIdNext}')">
					<strong>Pack (Next)</strong><br>
					<span>${elem.filename}</span><br>
					<span id="${resIdNext}" style="color: #4caf50; font-weight: bold; font-size: 0.75rem; display:block; margin-top:4px;">🔍 Chargement...</span>
				`;
				grid.appendChild(cardNext);

				const cardDefault = document.createElement('div');
				cardDefault.className = 'card-texture';
				cardDefault.onclick = () => systemOpen(defaultPath, 'file');
				const resIdDefault = `res-default-${elem.filename}`.replace(/[^a-zA-Z0-9]/g, '-');
				cardDefault.innerHTML = `
					<img src="${getAntiCacheUrl(defaultPath)}" onerror="handleImgError(this)" onload="afficherResolution(this, '${resIdDefault}')">
					<strong>Pack (Default)</strong><br>
					<span>${elem.filename}</span><br>
					<span id="${resIdDefault}" style="color: #4caf50; font-weight: bold; font-size: 0.75rem; display:block; margin-top:4px;">🔍 Chargement...</span>
				`;
				grid.appendChild(cardDefault);
			} else {
				document.getElementById('section-textures').style.display = 'none';
				chargerFichiersComparaisonDiff(mainPath, nextPath, defaultPath);
			}
		}

		function afficherResolution(img, spanId) {
			const span = document.getElementById(spanId);
			if (span) { span.innerText = `${img.naturalWidth}x${img.naturalHeight}`; }
		}

		function handleImgError(img) {
			img.src = './scripts/imgerror.svg';
			const card = img.closest('.card-texture');
			if(card) card.classList.add('has-error');
		}

		function toggleErrorsVisibility() {
			const body = document.body;
			const btn = document.getElementById('toggle-err-btn');
			if (body.classList.contains('hide-errors')) {
				body.classList.remove('hide-errors');
				btn.innerText = "Cacher les erreurs";
			} else {
				body.classList.add('hide-errors');
				btn.innerText = "Afficher les erreurs";
			}
		}

		function systemOpen(path, type) {
			const endpoint = type === 'file' ? 'open-file' : 'open-folder';
			fetch(`http://localhost:5000/${endpoint}`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ path: path })
			});
		}

		function runIndexer(scriptType) {
			const btn = event.target;
			const textOrigine = btn.innerText;
			
			let payload = {};
			if (scriptType === 'sounds') {
				const version = prompt("Entrez la version de Minecraft pour extraire les sons (ex: 1.18, 1.20) :", "1.18");
				if (version === null) return;
				if (version.trim() === "") {
					alert("Version invalide !");
					return;
				}
				payload.version = version.trim();
			}

			if (scriptType === 'pack_all') {
				btn.innerText = "⏳ Indexation des Blocs...";
				btn.disabled = true;

				fetch('http://localhost:5000/run/blocks', { method: 'POST' })
					.then(res => res.json())
					.then(data => {
						if (data.status === 'error') throw new Error("Blocs : " + data.message);
						btn.innerText = "⏳ Indexation des Items...";
						return fetch('http://localhost:5000/run/items', { method: 'POST' });
					})
					.then(res => res.json())
					.then(data => {
						if (data.status === 'error') throw new Error("Items : " + data.message);
						btn.innerText = "⏳ Indexation des Catégories...";
						return fetch('http://localhost:5000/run/categories', { method: 'POST' });
					})
					.then(res => res.json())
					.then(data => {
						if (data.status === 'error') throw new Error("Catégories : " + data.message);
						alert("🎉 Indexation du Pack terminée avec succès !");
						runUpdateFinish();
					})
					.catch(err => {
						alert("❌ " + err.message);
					})
					.finally(() => {
						btn.innerText = textOrigine;
						btn.disabled = false;
					});
				return;
			}

			btn.innerText = "⏳ Exécution...";
			btn.disabled = true;

			fetch(`http://localhost:5000/run/${scriptType}`, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			})
				.then(response => response.json())
				.then(data => {
					if (data.status === 'success') {
						alert("Opération terminée avec succès !");
						if (scriptType === 'comparaison') {
							chargerRapportComparaison();
						}
					} else {
						alert("Erreur : " + data.message);
					}
				})
				.catch(error => {
					console.error('Erreur:', error);
					alert("Impossible de joindre le serveur.");
				})
				.finally(() => {
					btn.innerText = textOrigine;
					btn.disabled = false;
				});
		}

		function runUpdateFinish() {
			console.log("Mise à jour de l'interface après indexation...");
			fetch('http://localhost:5000/get-index/blocks')
				.then(r => r.ok ? r.json() : [])
				.then(data => {
					dbBlocks = data;
					if (currentTab === 'blocks') genererListe(dbBlocks);
				});

			fetch('http://localhost:5000/get-index/items')
				.then(r => r.ok ? r.json() : [])
				.then(data => {
					dbItems = data;
					if (currentTab === 'items') genererListe(dbItems);
				});

			fetch('http://localhost:5000/get-categories')
				.then(r => r.ok ? r.json() : {})
				.then(categories => {
					dbCategories = categories;
					genererBoutonsCategories();
					if (currentTab !== 'blocks' && currentTab !== 'items' && currentTab !== 'comparaison') {
						genererListe(dbCategories[currentTab]);
					}
				});
		}

		function chargerRapportComparaison() {
			console.log("Tentative de chargement du rapport...");
			const timestamp = new Date().getTime();

			fetch(`http://localhost:5000/IndexData/Index_Comparaison_Missing.json?t=${timestamp}`)
				.then(response => {
					if (!response.ok) throw new Error("Fichier introuvable");
					return response.json();
				})
				.then(data => {
					dbComparaison = data; 
					console.log("Données chargées, éléments :", dbComparaison.length);
					switchTab('comparaison', false);
				})
				.catch(error => {
					console.error("Erreur :", error);
					alert("Erreur lors de l'affichage : " + error.message);
				});
		}

		function toggleTaskStatus() {
			if (!currentSelectedId || currentTab === 'comparaison') return;

			const index = completedTasks.indexOf(currentSelectedId);
			if (index > -1) {
				completedTasks.splice(index, 1);
			} else {
				completedTasks.push(currentSelectedId);
			}

			localStorage.setItem('mcp_completedTasks', JSON.stringify(completedTasks));
			
			if (currentTab === 'blocks' || currentTab === 'items') {
				genererListe(currentTab === 'blocks' ? dbBlocks : dbItems);
			} else {
				genererListe(dbCategories[currentTab]);
			}
			updateTaskButtonVisual();
		}

		function updateTaskButtonVisual() {
			const btn = document.getElementById('task-btn');
			if (currentTab === 'comparaison') {
				btn.style.display = 'none';
				return;
			}
			if (completedTasks.includes(currentSelectedId)) {
				btn.innerText = "❌ Annuler la validation";
				btn.className = "btn-check-task is-done";
			} else {
				btn.innerText = "✔️ Marquer comme fait";
				btn.className = "btn-check-task";
			}
		}

		function extraireFichierSelectionne() {
			if (!currentSelectedId) {
				alert("Veuillez sélectionner un fichier à extraire.");
				return;
			}
		
			let relativePath = currentSelectedId;
		
			const btn = document.getElementById('extract-single-btn');
			const oldText = btn.innerText;
			btn.innerText = "⏳ Extraction...";
			btn.disabled = true;
		
			fetch('http://localhost:5000/extract-single-file', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ path: relativePath })
			})
			.then(r => r.json())
			.then(data => {
				if (data.status === 'success') {
					alert("🎉 Fichier extrait avec succès dans le dossier Extract !");
				} else {
					alert("❌ Erreur : " + data.message);
				}
			})
			.catch(err => {
				alert("❌ Impossible de contacter le serveur.");
			})
			.finally(() => {
				btn.innerText = oldText;
				btn.disabled = false;
			});
		}
		
		window.togglePanneauElements = togglePanneauElements;