// js/dashboard.js

document.addEventListener("DOMContentLoaded", async () => {
    // ==========================================
    // 0. INTERNACIONALIZACIÓN 
    // ==========================================
    const traducciones = {
        es: {
            dashboard: "Panel de Control",
            userManagement: "Gestión de Usuarios",
            myAlarms: "Mis Alarmas",
            subtitle: "Gestiona tus zonas de monitorización activa.",
            syncing: "↻ Sincronizando...",
            synced: "● Sincronizado",
            error: "⚠ Error de conexión",
            mapHint: "Haz clic en el mapa para crear una alarma"
        },
        en: {
            dashboard: "Dashboard",
            userManagement: "User Management",
            myAlarms: "My Alarms",
            subtitle: "Manage your active monitoring zones.",
            syncing: "↻ Syncing...",
            synced: "● Synced",
            error: "⚠ Connection error",
            mapHint: "Click on the map to create an alarm"
        }
    };

    let idiomaActual = localStorage.getItem("lang") || "es";

    function aplicarTraduccion() {
        const t = traducciones[idiomaActual];
        
        document.getElementById("btnNavDashboard").childNodes[2].textContent = " " + t.dashboard;
        document.getElementById("btnNavAdmin").childNodes[2].textContent = " " + t.userManagement;
        
        const h1 = document.querySelector("h1");
        if(h1) h1.textContent = t.myAlarms;
        
        const pSubtitle = document.querySelector("p.text-sm.text-slate-400");
        if(pSubtitle) pSubtitle.textContent = t.subtitle;

        const txtLogoSubtitle = document.getElementById("txtLogoSubtitle");
        if(txtLogoSubtitle) txtLogoSubtitle.textContent = t.dashboard;

        const txtMapHint = document.getElementById("txtMapHint");
        if(txtMapHint) txtMapHint.textContent = t.mapHint;

        document.getElementById("langIcon").textContent = idiomaActual === "es" ? "ES" : "EN";
        localStorage.setItem("lang", idiomaActual);
        
        const badge = document.getElementById("txtMapStatus");
        if(badge && badge.innerText.includes("Sincron")) badge.innerText = t.syncing;
        else if(badge && badge.innerText.includes("incronizado")) badge.innerText = t.synced;
        else if(badge && badge.innerText.includes("Syncing")) badge.innerText = t.syncing;
        else if(badge && badge.innerText.includes("Synced")) badge.innerText = t.synced;
    }

    const btnToggleLang = document.getElementById("btnToggleLang");
    btnToggleLang.addEventListener("click", () => {
        idiomaActual = idiomaActual === "es" ? "en" : "es";
        aplicarTraduccion();
    });

    // ==========================================
    // 1. SEGURIDAD Y CONFIGURACIÓN INICIAL
    // ==========================================
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    if (!token) {
        window.location.href = "index.html";
        return;
    }

    const txtUserEmail = document.getElementById("txtUserEmail");
    const txtUserRole = document.getElementById("txtUserRole");
    const btnNavDashboard = document.getElementById("btnNavDashboard");
    const btnNavAdmin = document.getElementById("btnNavAdmin");
    const panelUser = document.getElementById("panelUser");
    const panelAdmin = document.getElementById("panelAdmin");
    const btnLogout = document.getElementById("btnLogout");

    let emailSesion = localStorage.getItem("email") || localStorage.getItem("user_email");
    
    if (!emailSesion && token) {
        try {
            const base64Url = token.split('.')[1];
            const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
            const payload = JSON.parse(window.atob(base64));
            emailSesion = payload.sub || payload.email || payload.username;
        } catch (e) {
            console.error("No se pudo extraer el email del token de seguridad", e);
        }
    }
    
    if (!emailSesion) {
        emailSesion = "usuario@ringhere.com";
    }
    
    txtUserEmail.innerText = emailSesion;
    txtUserRole.innerText = role;

    if (role === "admin") {
        btnNavAdmin.classList.remove("hidden");
    }

    // ==========================================
    // 2. MODO OSCURO GLOBAL
    // ==========================================
    const btnToggleTheme = document.getElementById("btnToggleTheme");
    const themeIcon = document.getElementById("themeIcon");
    const htmlElement = document.documentElement;

    const svgMoon = `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"></path></svg>`;
    const svgSun = `<svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"></path></svg>`;

    const darkMapStyle = [
        { elementType: "geometry", stylers: [{ color: "#242f3e" }] },
        { elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
        { elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
        { featureType: "administrative.locality", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
        { featureType: "poi", elementType: "labels.text.fill", stylers: [{ color: "#d59563" }] },
        { featureType: "poi.park", elementType: "geometry", stylers: [{ color: "#263c3f" }] },
        { featureType: "poi.park", elementType: "labels.text.fill", stylers: [{ color: "#6b9a76" }] },
        { featureType: "road", elementType: "geometry", stylers: [{ color: "#38414e" }] },
        { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#212a37" }] },
        { featureType: "road", elementType: "labels.text.fill", stylers: [{ color: "#9ca5b3" }] },
        { featureType: "water", elementType: "geometry", stylers: [{ color: "#17263c" }] }
    ];

    if (localStorage.getItem("theme") === "dark") {
        htmlElement.classList.add("dark");
        themeIcon.innerHTML = svgSun;
    } else {
        themeIcon.innerHTML = svgMoon;
    }

    btnToggleTheme.addEventListener("click", () => {
        const isDark = htmlElement.classList.contains("dark");
        if (isDark) {
            htmlElement.classList.remove("dark");
            localStorage.setItem("theme", "light");
            themeIcon.innerHTML = svgMoon;
            if (mapaGlobal) mapaGlobal.setOptions({ styles: [] }); 
        } else {
            htmlElement.classList.add("dark");
            localStorage.setItem("theme", "dark");
            themeIcon.innerHTML = svgSun;
            if (mapaGlobal) mapaGlobal.setOptions({ styles: darkMapStyle }); 
        }
    });

    // ==========================================
    // 3. SISTEMA DE GOOGLE MAPS
    // ==========================================
    let mapaGlobal = null;
    let capasCirculos = []; 

    let marcadorBorrador = null;
    let circuloBorrador = null;
    let ubicacionBorrador = { lat: null, lng: null };

    const btnConfirmLocation = document.getElementById("btnConfirmLocation");
    const sliderRadius = document.getElementById("sliderRadius");
    const txtRadiusValue = document.getElementById("txtRadiusValue");

    function activarEventosMapa() {
        if (!mapaGlobal) return;
        
        mapaGlobal.addListener("click", (e) => {
            const lat = e.latLng.lat();
            const lng = e.latLng.lng();
            ubicacionBorrador = { lat, lng };

            if (marcadorBorrador) marcadorBorrador.setMap(null);
            if (circuloBorrador) circuloBorrador.setMap(null);

            const borradorSvg = {
                path: "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
                fillColor: "#06b6d4",
                fillOpacity: 1,
                strokeWeight: 1,
                strokeColor: "#ffffff",
                rotation: 0,
                scale: 1.5,
                anchor: new google.maps.Point(12, 22)
            };

            marcadorBorrador = new google.maps.Marker({
                position: ubicacionBorrador,
                map: mapaGlobal,
                icon: borradorSvg,
                animation: google.maps.Animation.DROP
            });

            const radioInicial = parseInt(sliderRadius.value) || 450;
            circuloBorrador = new google.maps.Circle({
                strokeColor: '#06b6d4', 
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillColor: '#06b6d4', 
                fillOpacity: 0.3,
                map: mapaGlobal,
                center: ubicacionBorrador,
                radius: radioInicial
            });

            btnConfirmLocation.classList.remove("hidden");
        });
    }

    function renderizarMapaYAlarmas(alarmas) {
        if (!mapaGlobal) {
            const isDark = htmlElement.classList.contains("dark");
            mapaGlobal = new google.maps.Map(document.getElementById("map"), {
                center: { lat: 40.3233, lng: -3.8676 }, 
                zoom: 13,
                mapTypeControl: false,
                streetViewControl: false,
                fullscreenControl: false,
                styles: isDark ? darkMapStyle : [] 
            });

            activarEventosMapa();
        }

        capasCirculos.forEach(capa => capa.setMap(null));
        capasCirculos = [];

        if (!alarmas || alarmas.length === 0) return;

        const limites = new google.maps.LatLngBounds();
        let hayMarcadores = false;

        alarmas.forEach(alarma => {
            const lat = parseFloat(alarma.latitud || alarma.lat);
            const lng = parseFloat(alarma.longitud || alarma.lng);
            const radio = parseFloat(alarma.radio) || 100;

            if (!isNaN(lat) && !isNaN(lng)) {
                hayMarcadores = true;
                const centro = { lat, lng };
                
                let colorHex = '#94a3b8'; 
                if (alarma.is_active) {
                    colorHex = alarma.is_al_entrar ? '#3b82f6' : '#ef4444'; 
                }

                const pinSvg = {
                    path: "M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z",
                    fillColor: colorHex,
                    fillOpacity: 1,
                    strokeWeight: 1,
                    strokeColor: "#ffffff",
                    rotation: 0,
                    scale: 1.5,
                    anchor: new google.maps.Point(12, 22)
                };

                const marcador = new google.maps.Marker({
                    position: centro,
                    map: mapaGlobal,
                    title: alarma.nombre || 'Alarma',
                    icon: pinSvg
                });

                // Creación del InfoWindow (Bocadillo interactivo)
                const textoTipo = alarma.is_al_entrar ? "Alarma al entrar" : "Alarma al salir";
                const infoWindow = new google.maps.InfoWindow({
                    content: `
                        <div style="padding: 4px; text-align: center; min-width: 120px;">
                            <p style="color: #3b82f6; font-family: ui-sans-serif, system-ui, sans-serif; font-weight: 800; font-size: 15px; margin: 0; padding-bottom: 4px;">
                                ${alarma.nombre || 'Alarma'}
                            </p>
                            <p style="color: #64748b; font-family: ui-sans-serif, system-ui, sans-serif; font-size: 12px; margin: 0;">
                                ${textoTipo}
                            </p>
                        </div>
                    `
                });

                // Evento para abrir el bocadillo al clicar en la chincheta
                marcador.addListener("click", () => {
                    infoWindow.open({
                        anchor: marcador,
                        map: mapaGlobal,
                    });
                });

                const circulo = new google.maps.Circle({
                    strokeColor: colorHex,
                    strokeOpacity: 0.8,
                    strokeWeight: 2,
                    fillColor: colorHex,
                    fillOpacity: 0.2,
                    map: mapaGlobal,
                    center: centro,
                    radius: radio
                });

                capasCirculos.push(marcador, circulo);
                limites.extend(centro);
            }
        });

        if (hayMarcadores) {
            mapaGlobal.fitBounds(limites);
        }
    }

    const btnToggleMapMenu = document.getElementById("btnToggleMapMenu");
    const mapTypeMenu = document.getElementById("mapTypeMenu");
    const btnsMapType = document.querySelectorAll(".btn-map-type");

    if (btnToggleMapMenu && mapTypeMenu) {
        btnToggleMapMenu.addEventListener("click", (e) => {
            e.stopPropagation();
            mapTypeMenu.classList.toggle("hidden");
        });

        btnsMapType.forEach(btn => {
            btn.addEventListener("click", (e) => {
                if (!mapaGlobal) return;
                const nuevoTipo = e.target.getAttribute("data-type");
                mapaGlobal.setMapTypeId(nuevoTipo);
                mapTypeMenu.classList.add("hidden");
            });
        });

        document.addEventListener("click", (e) => {
            if (!mapTypeMenu.classList.contains("hidden") && !mapTypeMenu.contains(e.target) && e.target !== btnToggleMapMenu) {
                mapTypeMenu.classList.add("hidden");
            }
        });
    }

    // ==========================================
    // 4. LÓGICA DE CREACIÓN (MODAL Y POST)
    // ==========================================
    const modalNewAlarm = document.getElementById("modalNewAlarm");
    const btnCloseModal = document.getElementById("btnCloseModal");
    const btnsTriggerType = document.querySelectorAll(".btn-trigger-type");
    const btnSaveAlarm = document.getElementById("btnSaveAlarm");
    const inputAlarmName = document.getElementById("inputAlarmName");

    let triggerTypeSeleccionado = "entrar";

    btnConfirmLocation.addEventListener("click", () => {
        modalNewAlarm.classList.remove("hidden");
        setTimeout(() => {
            modalNewAlarm.classList.remove("opacity-0");
            modalNewAlarm.firstElementChild.classList.remove("scale-95");
        }, 10);
    });

    function cerrarModal() {
        modalNewAlarm.classList.add("opacity-0");
        modalNewAlarm.firstElementChild.classList.add("scale-95");
        setTimeout(() => {
            modalNewAlarm.classList.add("hidden");
            inputAlarmName.value = "";
        }, 300);
    }
    btnCloseModal.addEventListener("click", cerrarModal);

    sliderRadius.addEventListener("input", (e) => {
        const nuevoRadio = parseInt(e.target.value);
        txtRadiusValue.innerText = nuevoRadio;
        if (circuloBorrador) {
            circuloBorrador.setRadius(nuevoRadio);
        }
    });

    btnsTriggerType.forEach(btn => {
        btn.addEventListener("click", (e) => {
            btnsTriggerType.forEach(b => {
                b.className = "btn-trigger-type flex-1 py-3 px-4 rounded-2xl font-bold text-sm bg-white dark:bg-slate-800 text-slate-600 dark:text-slate-300 border-2 border-slate-200 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700 transition-all";
            });
            const target = e.currentTarget;
            target.className = "btn-trigger-type flex-1 py-3 px-4 rounded-2xl font-bold text-sm bg-blue-800 text-white border-2 border-blue-800 transition-all shadow-md shadow-blue-200 dark:shadow-none";
            triggerTypeSeleccionado = target.getAttribute("data-type");
        });
    });

    btnSaveAlarm.addEventListener("click", async () => {
        const nombre = inputAlarmName.value.trim();
        if (!nombre) {
            alert("Por favor, introduce un nombre para la alarma.");
            return;
        }

        const idUsuario = parseInt(localStorage.getItem("user_id") || localStorage.getItem("usuario_id") || localStorage.getItem("id")) || 1;

        const dataAlarma = {
            id: crypto.randomUUID(),  
            nombre: nombre, 
            latitud: ubicacionBorrador.lat,
            longitud: ubicacionBorrador.lng,
            radio: parseInt(sliderRadius.value),
            is_active: true,
            is_al_entrar: triggerTypeSeleccionado === "entrar", 
            fecha_creacion: Date.now(), 
            user_email: emailSesion, 
            usuario_id: idUsuario
        };

        btnSaveAlarm.innerText = "Guardando...";
        
        try {
            await API.post("/alarmas/", dataAlarma);
            
            cerrarModal();
            if (marcadorBorrador) marcadorBorrador.setMap(null);
            if (circuloBorrador) circuloBorrador.setMap(null);
            btnConfirmLocation.classList.add("hidden");
            
            btnSaveAlarm.innerHTML = `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg> Guardar Alarma`;
            await cargarAlarmasUsuario();
            
        } catch (error) {
            console.error("Error al guardar alarma:", error);
            let msg = error.message;
            if (typeof msg === 'object') msg = JSON.stringify(msg, null, 2);
            alert("Error al guardar en el servidor: " + msg);
            btnSaveAlarm.innerHTML = `<svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"></path></svg> Guardar Alarma`;
        }
    });
    
    // ==========================================
    // 5. DATOS Y CRUD (LECTURA, EDICIÓN Y BORRADO)
    // ==========================================
    function setSyncStatus(state) {
        const badge = document.getElementById("txtMapStatus");
        if (!badge) return;
        const t = traducciones[idiomaActual];

        if (state === 'syncing') {
            badge.className = "text-xs px-2.5 py-1 bg-yellow-100 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400 font-semibold rounded-full animate-pulse";
            badge.innerText = t.syncing;
        } else if (state === 'live') {
            badge.className = "text-xs px-2.5 py-1 bg-emerald-100 dark:bg-emerald-900/30 text-emerald-800 dark:text-emerald-400 font-semibold rounded-full transition-all";
            badge.innerText = t.synced;
        } else if (state === 'error') {
            badge.className = "text-xs px-2.5 py-1 bg-red-100 dark:bg-red-900/30 text-red-800 dark:text-red-400 font-semibold rounded-full transition-all";
            badge.innerText = t.error;
        }
    }

    async function cargarContenido() {
        try {
            if (role === "admin" && !panelAdmin.classList.contains("hidden")) {
                await cargarUsuariosAdmin();
            } else {
                await cargarAlarmasUsuario();
            }
        } catch (error) {
            console.error("Error cargando panel:", error.message);
        }
    }

    async function cargarAlarmasUsuario() {
        const alarmasContainer = document.getElementById("alarmasContainer");
        setSyncStatus('syncing'); 

        try {
            const alarmas = await API.get("/alarmas/");

            if (alarmas.length === 0) {
                alarmasContainer.innerHTML = `<p class="text-sm text-slate-400">No tienes ninguna alarma creada todavía. ¡Haz clic en el mapa para empezar!</p>`;
                renderizarMapaYAlarmas([]); 
                setSyncStatus('live'); 
                return;
            }

            alarmasContainer.innerHTML = alarmas.map(alarma => {
                const isEntrar = alarma.is_al_entrar;
                const colorActivoClase = isEntrar ? 'peer-checked:bg-blue-500' : 'peer-checked:bg-red-500';
                const badgeColorClase = !alarma.is_active ? 'bg-slate-400' : (isEntrar ? 'bg-blue-500' : 'bg-red-500');
                const textoTipo = isEntrar ? 'Al entrar' : 'Al salir';

                return `
                <div class="bg-white dark:bg-slate-800 p-5 border border-slate-100 dark:border-slate-700 rounded-2xl shadow-sm flex justify-between items-center transition-colors duration-300">
                    <div>
                        <h4 class="font-bold text-slate-800 dark:text-white flex items-center gap-2">
                            <span id="badge-alarma-${alarma.id}" class="w-2.5 h-2.5 rounded-full transition-colors ${badgeColorClase}"></span>
                            ${alarma.nombre || alarma.titulo || 'Alarma sin título'}
                        </h4>
                        <p class="text-xs text-slate-400 dark:text-slate-400 mt-1 pl-4 flex items-center gap-1">
                            <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                            Radio: ${alarma.radio !== undefined ? Math.round(alarma.radio) : '—'}m • ${textoTipo}
                        </p>
                    </div>
                    
                    <div class="flex items-center gap-4">
                        <label class="relative inline-flex items-center cursor-pointer" title="${alarma.is_active ? 'Desactivar' : 'Activar'} alarma">
                            <input type="checkbox" class="sr-only peer toggle-alarma" data-id="${alarma.id}" ${alarma.is_active ? 'checked' : ''}>
                            <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer dark:bg-slate-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 ${colorActivoClase}"></div>
                        </label>
                        <div class="w-px h-6 bg-slate-200 dark:bg-slate-700"></div>
                        <button class="btn-delete-alarma p-2 text-slate-400 hover:text-red-600 dark:hover:text-red-400 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-all" data-id="${alarma.id}" title="Eliminar alarma">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </div>
                </div>
                `;
            }).join('');

            renderizarMapaYAlarmas(alarmas);
            setSyncStatus('live'); 

            document.querySelectorAll(".toggle-alarma").forEach(toggle => {
                toggle.addEventListener("change", async (e) => {
                    const alarmaId = e.target.getAttribute("data-id");
                    const nuevoEstado = e.target.checked; 
                    setSyncStatus('syncing'); 
                    try {
                        await API.put(`/alarmas/${alarmaId}`, { is_active: nuevoEstado });
                        const alarmaEditada = alarmas.find(a => String(a.id) === String(alarmaId));
                        if (alarmaEditada) {
                            alarmaEditada.is_active = nuevoEstado;
                            const badge = document.getElementById(`badge-alarma-${alarmaId}`);
                            if (badge) {
                                const isEntrar = alarmaEditada.is_al_entrar;
                                badge.className = `w-2.5 h-2.5 rounded-full transition-colors ${!nuevoEstado ? 'bg-slate-400' : (isEntrar ? 'bg-blue-500' : 'bg-red-500')}`;
                            }
                            renderizarMapaYAlarmas(alarmas);
                            setSyncStatus('live'); 
                        }
                    } catch (error) {
                        setSyncStatus('error'); 
                        alert("Error al sincronizar el estado: " + error.message);
                        e.target.checked = !nuevoEstado; 
                    }
                });
            });

            document.querySelectorAll(".btn-delete-alarma").forEach(btn => {
                btn.addEventListener("click", async (e) => {
                    const alarmaId = e.currentTarget.getAttribute("data-id");
                    if (confirm("¿Estás seguro de que deseas eliminar esta alarma? Desaparecerá permanentemente de tu cuenta.")) {
                        setSyncStatus('syncing'); 
                        try {
                            await API.delete(`/alarmas/${alarmaId}`);
                            await cargarAlarmasUsuario(); 
                        } catch (error) {
                            setSyncStatus('error'); 
                            alert("Error al eliminar la alarma: " + error.message);
                        }
                    }
                });
            });

        } catch (error) {
            setSyncStatus('error'); 
            alarmasContainer.innerHTML = `<p class="text-sm text-red-500 font-medium"><svg class="w-4 h-4 inline-block mr-1 -mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg> Error de red: ${error.message}</p>`;
        }
    }

    async function cargarUsuariosAdmin() {
        const tbody = document.getElementById("usuariosTableBody");
        const statTotalUsers = document.getElementById("statTotalUsers");
        const statTotalAdmins = document.getElementById("statTotalAdmins");

        try {
            const usuarios = await API.get("/usuarios/");
            statTotalUsers.innerText = usuarios.length;
            statTotalAdmins.innerText = usuarios.filter(u => u.role === "admin").length;

            tbody.innerHTML = usuarios.map(u => `
                <tr class="hover:bg-slate-50/50 dark:hover:bg-slate-700/30 transition-all">
                    <td class="p-4 pl-6 font-mono text-xs text-blue-600 dark:text-blue-400 font-semibold">#USR-${u.id}</td>
                    <td class="p-4 font-medium text-slate-900 dark:text-white">${u.full_name || 'Sin nombre'}</td>
                    <td class="p-4 text-slate-500 dark:text-slate-400">${u.email}</td>
                    <td class="p-4">
                        <select data-id="${u.id}" class="select-change-role border border-slate-200 dark:border-slate-600 rounded-xl text-xs font-bold px-2.5 py-1.5 focus:ring-2 focus:ring-blue-600 outline-none cursor-pointer transition-all ${u.role === 'admin' ? 'text-indigo-800 bg-indigo-50 dark:bg-indigo-900/30 dark:text-indigo-300' : 'text-slate-600 bg-slate-50 dark:bg-slate-700 dark:text-slate-200'}">
                            <option value="user" ${u.role === 'user' ? 'selected' : ''}>user</option>
                            <option value="admin" ${u.role === 'admin' ? 'selected' : ''}>admin</option>
                        </select>
                    </td>
                    <td class="p-4 text-center">
                        <button data-id="${u.id}" class="btn-delete-user p-2 text-slate-400 hover:text-red-600 dark:hover:text-red-400 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-all" title="Eliminar usuario">
                            <svg class="w-5 h-5 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                        </button>
                    </td>
                </tr>
            `).join('');

            document.querySelectorAll(".select-change-role").forEach(select => {
                select.addEventListener("change", async (e) => {
                    const userId = e.target.getAttribute("data-id");
                    const nuevoRol = e.target.value;
                    try {
                        await API.put(`/usuarios/${userId}/rol`, { role: nuevoRol });
                        await cargarUsuariosAdmin();
                    } catch (err) {
                        alert(`Error: ${err.message}`);
                        await cargarUsuariosAdmin();
                    }
                });
            });

            document.querySelectorAll(".btn-delete-user").forEach(btn => {
                btn.addEventListener("click", async (e) => {
                    const userId = e.currentTarget.getAttribute("data-id");
                    if (confirm(`¿Estás completamente seguro de eliminar al usuario #${userId}?`)) {
                        try {
                            await API.delete(`/usuarios/${userId}`);
                            await cargarUsuariosAdmin();
                        } catch (err) {
                            alert(`Error: ${err.message}`);
                        }
                    }
                });
            });

        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="5" class="p-6 text-center text-red-500 font-medium">
                <svg class="w-5 h-5 inline-block mr-2 -mt-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                Error de autorización: ${error.message}
            </td></tr>`;
        }
    }

    // ==========================================
    // 6. INTERACCIÓN DE NAVEGACIÓN
    // ==========================================
    function alternarEstilosMenu(pestanaActiva, pestanaInactiva) {
        pestanaActiva.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl bg-blue-800 text-white shadow-md shadow-blue-100 dark:shadow-none transition-all";
        // FIX: preservar "hidden" si el elemento era invisible (ej. btnNavAdmin para role=user)
        const eraOculto = pestanaInactiva.classList.contains("hidden");
        pestanaInactiva.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-all";
        if (eraOculto) pestanaInactiva.classList.add("hidden");
    }

    btnNavDashboard.addEventListener("click", () => {
        alternarEstilosMenu(btnNavDashboard, btnNavAdmin);
        panelUser.classList.remove("hidden");
        panelAdmin.classList.add("hidden");
        cargarContenido();
    });

    btnNavAdmin.addEventListener("click", () => {
        if (role !== "admin") return;
        alternarEstilosMenu(btnNavAdmin, btnNavDashboard);
        panelAdmin.classList.remove("hidden");
        panelUser.classList.add("hidden");
        cargarContenido();
    });

    btnLogout.addEventListener("click", () => {
        if (confirm("¿Cerrar sesión?")) {
            localStorage.clear();
            window.location.href = "index.html";
        }
    });

    aplicarTraduccion();
    await cargarContenido();
});