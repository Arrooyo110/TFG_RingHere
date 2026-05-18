// js/dashboard.js

document.addEventListener("DOMContentLoaded", async () => {
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

    // 🔍 CORRECCIÓN: Buscamos el email real en localStorage (admite "email" o "user_email")
    const emailSesion = localStorage.getItem("email") || localStorage.getItem("user_email") || "prueba@gmail.com";
    
    // Asignamos el email real al perfil visual abajo a la izquierda
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
        themeIcon.innerText = "☀️";
    }

    btnToggleTheme.addEventListener("click", () => {
        const isDark = htmlElement.classList.contains("dark");
        if (isDark) {
            htmlElement.classList.remove("dark");
            localStorage.setItem("theme", "light");
            themeIcon.innerText = "🌙";
            if (mapaGlobal) mapaGlobal.setOptions({ styles: [] }); 
        } else {
            htmlElement.classList.add("dark");
            localStorage.setItem("theme", "dark");
            themeIcon.innerText = "☀️";
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

            marcadorBorrador = new google.maps.Marker({
                position: ubicacionBorrador,
                map: mapaGlobal,
                icon: "http://maps.google.com/mapfiles/ms/icons/blue-dot.png",
                animation: google.maps.Animation.DROP
            });

            const radioInicial = parseInt(sliderRadius.value) || 450;
            circuloBorrador = new google.maps.Circle({
                strokeColor: '#3b82f6', 
                strokeOpacity: 0.8,
                strokeWeight: 2,
                fillColor: '#60a5fa', 
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
                const colorHex = alarma.is_active ? '#10b981' : '#94a3b8';

                const marcador = new google.maps.Marker({
                    position: centro,
                    map: mapaGlobal,
                    title: alarma.nombre || 'Alarma'
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
    const btnCreateNewAlarmHeader = document.getElementById("btnCreateNewAlarmHeader");

    let triggerTypeSeleccionado = "entrar";

    if (btnCreateNewAlarmHeader) {
        btnCreateNewAlarmHeader.addEventListener("click", () => {
            alert("Haz clic en cualquier parte del mapa para colocar la chincheta de tu nueva alarma.");
        });
    }

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

    // Guardar en la Base de Datos (POST) - CONFIGURACIÓN EN PARIDAD CON KOTLIN
    btnSaveAlarm.addEventListener("click", async () => {
        const nombre = inputAlarmName.value.trim();
        if (!nombre) {
            alert("Por favor, introduce un nombre para la alarma.");
            return;
        }

        const emailUsuario = localStorage.getItem("email") || localStorage.getItem("user_email") || "prueba@gmail.com";
        const idUsuario = parseInt(localStorage.getItem("user_id") || localStorage.getItem("usuario_id") || localStorage.getItem("id")) || 1;

        // CONSTRUIMOS EL OBJETO REPLICANDO LA DATA CLASS DE KOTLIN
        const dataAlarma = {
            id: crypto.randomUUID(),  // 🚀 Genera un UUID v4 idéntico al 'UUID.randomUUID()' de Android
            nombre: nombre, 
            latitud: ubicacionBorrador.lat,
            longitud: ubicacionBorrador.lng,
            radio: parseInt(sliderRadius.value),
            is_active: true,
            is_al_entrar: triggerTypeSeleccionado === "entrar", // Mapea los botones del Modal
            fecha_creacion: Date.now(), // ⏱️ Timestamp en milisegundos igual que 'System.currentTimeMillis()'
            user_email: emailUsuario, 
            usuario_id: idUsuario
        };

        btnSaveAlarm.innerText = "Guardando...";
        
        try {
            // Enviamos el objeto perfecto a tu API
            await API.post("/alarmas/", dataAlarma);
            
            cerrarModal();
            if (marcadorBorrador) marcadorBorrador.setMap(null);
            if (circuloBorrador) circuloBorrador.setMap(null);
            btnConfirmLocation.classList.add("hidden");
            
            btnSaveAlarm.innerHTML = "<span>🔔</span> Guardar Alarma";
            
            // Refrescamos la lista para ver el resultado con su switch interactivo
            await cargarAlarmasUsuario();
            
        } catch (error) {
            console.error("Error al guardar alarma:", error);
            let msg = error.message;
            if (typeof msg === 'object') msg = JSON.stringify(msg, null, 2);
            alert("Error al guardar en el servidor: " + msg);
            btnSaveAlarm.innerHTML = "<span>🔔</span> Guardar Alarma";
        }
    });
    
    // ==========================================
    // 5. DATOS Y CRUD (LECTURA, EDICIÓN Y BORRADO)
    // ==========================================
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
        try {
            const alarmas = await API.get("/alarmas/");

            if (alarmas.length === 0) {
                alarmasContainer.innerHTML = `<p class="text-sm text-slate-400">No tienes ninguna alarma creada todavía. ¡Haz clic en el mapa para empezar!</p>`;
                renderizarMapaYAlarmas([]); 
                return;
            }

            alarmasContainer.innerHTML = alarmas.map(alarma => `
                <div class="bg-white dark:bg-slate-800 p-5 border border-slate-100 dark:border-slate-700 rounded-2xl shadow-sm flex justify-between items-center transition-colors duration-300">
                    <div>
                        <h4 class="font-bold text-slate-800 dark:text-white">${alarma.nombre || alarma.titulo || 'Alarma sin título'}</h4>
                        <p class="text-xs text-slate-400 dark:text-slate-400 mt-1">📍 Radio: ${alarma.radio !== undefined ? Math.round(alarma.radio) : '—'}m</p>
                    </div>
                    
                    <div class="flex items-center gap-4">
                        <label class="relative inline-flex items-center cursor-pointer" title="${alarma.is_active ? 'Desactivar' : 'Activar'} alarma">
                            <input type="checkbox" class="sr-only peer toggle-alarma" data-id="${alarma.id}" ${alarma.is_active ? 'checked' : ''}>
                            <div class="w-11 h-6 bg-slate-200 peer-focus:outline-none rounded-full peer dark:bg-slate-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-slate-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-slate-600 peer-checked:bg-emerald-500"></div>
                        </label>
                        <div class="w-px h-6 bg-slate-200 dark:bg-slate-700"></div>
                        <button class="btn-delete-alarma p-2 text-slate-400 hover:text-red-600 dark:hover:text-red-400 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 transition-all" data-id="${alarma.id}" title="Eliminar alarma">
                            🗑️
                        </button>
                    </div>
                </div>
            `).join('');

            renderizarMapaYAlarmas(alarmas);

            document.querySelectorAll(".toggle-alarma").forEach(toggle => {
                toggle.addEventListener("change", async (e) => {
                    const alarmaId = e.target.getAttribute("data-id");
                    const nuevoEstado = e.target.checked; 
                    try {
                        await API.put(`/alarmas/${alarmaId}`, { is_active: nuevoEstado });
                        const alarmaEditada = alarmas.find(a => String(a.id) === String(alarmaId));
                        if (alarmaEditada) {
                            alarmaEditada.is_active = nuevoEstado;
                            renderizarMapaYAlarmas(alarmas); 
                        }
                    } catch (error) {
                        alert("Error al sincronizar el estado: " + error.message);
                        e.target.checked = !nuevoEstado; 
                    }
                });
            });

            document.querySelectorAll(".btn-delete-alarma").forEach(btn => {
                btn.addEventListener("click", async (e) => {
                    const alarmaId = e.currentTarget.getAttribute("data-id");
                    if (confirm("¿Estás seguro de que deseas eliminar esta alarma? Desaparecerá permanentemente de tu cuenta.")) {
                        try {
                            await API.delete(`/alarmas/${alarmaId}`);
                            await cargarAlarmasUsuario();
                        } catch (error) {
                            alert("Error al eliminar la alarma: " + error.message);
                        }
                    }
                });
            });

        } catch (error) {
            alarmasContainer.innerHTML = `<p class="text-sm text-red-500"> Error de red: ${error.message}</p>`;
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
                            🗑️
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
            tbody.innerHTML = `<tr><td colspan="5" class="p-6 text-center text-red-500">❌ Error de autorización: ${error.message}</td></tr>`;
        }
    }

    // ==========================================
    // 6. INTERACCIÓN DE NAVEGACIÓN
    // ==========================================
    function alternarEstilosMenu(pestanaActiva, pestanaInactiva) {
        pestanaActiva.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl bg-blue-800 text-white shadow-md shadow-blue-100 dark:shadow-none transition-all";
        pestanaInactiva.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800 hover:text-slate-900 dark:hover:text-white transition-all";
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

    // Arranque
    await cargarContenido();
});