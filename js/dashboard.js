// js/dashboard.js

document.addEventListener("DOMContentLoaded", async () => {
    // 1. ESCUDO DE SEGURIDAD EN FRONTEND: ¿Hay sesión activa?
    const token = localStorage.getItem("token");
    const role = localStorage.getItem("role");

    if (!token) {
        // Si no hay token, redirigimos inmediatamente al login
        window.location.href = "index.html";
        return;
    }

    // 2. CONFIGURACIÓN INICIAL DE LA INTERFAZ
    const txtUserEmail = document.getElementById("txtUserEmail");
    const txtUserRole = document.getElementById("txtUserRole");
    const btnNavDashboard = document.getElementById("btnNavDashboard");
    const btnNavAdmin = document.getElementById("btnNavAdmin");
    const panelUser = document.getElementById("panelUser");
    const panelAdmin = document.getElementById("panelAdmin");
    const btnLogout = document.getElementById("btnLogout");

    // Rellenamos los datos del perfil abajo en el sidebar
    // Nota: Como no guardamos el email explícito en localStorage, usamos un genérico o puedes guardarlo al hacer login
    txtUserEmail.innerText = role === "admin" ? "admin@ringhere.com" : "user@ringhere.com";
    txtUserRole.innerText = role;

    // 3. CONTROL DE ROLES INTELEGENTE
    if (role === "admin") {
        // Si es admin, hacemos visible el botón de gestión de usuarios en el menú
        btnNavAdmin.classList.remove("hidden");
    }

    // 4. LÓGICA DE CARGA DE DATOS SEGÚN EL ROL
    async function cargarContenido() {
        try {
            if (role === "admin" && !panelAdmin.classList.contains("hidden")) {
                // Estamos en la vista de administración -> Cargar Tabla de Usuarios
                await cargarUsuariosAdmin();
            } else {
                // Estamos en la vista estándar -> Cargar Alarmas del usuario
                await cargarAlarmasUsuario();
            }
        } catch (error) {
            console.error("Error cargando contenido del panel:", error.message);
        }
    }

    // --- CARGAR ALARMAS (Vista de Usuario) ---
    async function cargarAlarmasUsuario() {
        const alarmasContainer = document.getElementById("alarmasContainer");
        try {
            // Llamamos al endpoint GET /alarmas/ que programamos en tu FastAPI
            const alarmas = await API.get("/alarmas/");
            
            if (alarmas.length === 0) {
                alarmasContainer.innerHTML = `<p class="text-sm text-slate-400">No tienes ninguna alarma creada todavía. ¡Crea una desde la app móvil o el PC!</p>`;
                return;
            }

            // Pintamos las tarjetas dinámicamente con estilos Tailwind idénticos a Figma
            alarmasContainer.innerHTML = alarmas.map(alarma => `
                <div class="bg-white p-5 border border-slate-100 rounded-2xl shadow-sm flex justify-between items-center">
                    <div>
                        <h4 class="font-bold text-slate-800">${alarma.title || 'Alarma sin título'}</h4>
                        <p class="text-xs text-slate-400 mt-1">📍 Radio: ${alarma.radius}m</p>
                    </div>
                    <div class="flex items-center gap-3">
                        <span class="px-2.5 py-1 text-xs font-semibold rounded-full ${alarma.is_active ? 'bg-emerald-100 text-emerald-800' : 'bg-slate-100 text-slate-500'}">
                            ${alarma.is_active ? 'Monitoring' : 'Paused'}
                        </span>
                    </div>
                </div>
            `).join('');

        } catch (error) {
            alarmasContainer.innerHTML = `<p class="text-sm text-red-500">❌ Error al conectar con Render: ${error.message}</p>`;
        }
    }

    // --- CARGAR USUARIOS (Vista de Administrador) ---
    async function cargarUsuariosAdmin() {
        const tbody = document.getElementById("usuariosTableBody");
        const statTotalUsers = document.getElementById("statTotalUsers");
        const statTotalAdmins = document.getElementById("statTotalAdmins");

        try {
            // Llamamos al endpoint superprotegido GET /usuarios/
            const usuarios = await API.get("/usuarios/");
            
            // Actualizamos tarjetas de estadísticas
            statTotalUsers.innerText = usuarios.length;
            const admins = usuarios.filter(u => u.role === "admin").length;
            statTotalAdmins.innerText = admins;

            // Rellenamos la tabla dinámicamente
            tbody.innerHTML = usuarios.map(u => `
                <tr class="hover:bg-slate-50/50 transition-all">
                    <td class="p-4 pl-6 font-mono text-xs text-blue-600 font-semibold">#USR-${u.id}</td>
                    <td class="p-4 font-medium text-slate-900">${u.full_name || 'Sin nombre'}</td>
                    <td class="p-4 text-slate-500">${u.email}</td>
                    <td class="p-4">
                        <span class="px-2.5 py-0.5 text-xs font-semibold rounded-full ${u.role === 'admin' ? 'bg-indigo-100 text-indigo-800' : 'bg-slate-100 text-slate-600'}">
                            ${u.role}
                        </span>
                    </td>
                    <td class="p-4 text-center">
                        <button data-id="${u.id}" class="btn-delete-user p-2 text-slate-400 hover:text-red-600 rounded-xl hover:bg-red-50 transition-all" title="Eliminar usuario">
                            🗑️
                        </button>
                    </td>
                </tr>
            `).join('');

            // Asignamos los eventos de borrado a los botones de la tabla recién creados
            document.querySelectorAll(".btn-delete-user").forEach(btn => {
                btn.addEventListener("click", async (e) => {
                    const userId = e.currentTarget.getAttribute("data-id");
                    if (confirm(`¿Estás completamente seguro de eliminar al usuario con ID ${userId}? Esta acción destruirá su cuenta de forma permanente en Neon.`)) {
                        try {
                            // Ejecutamos DELETE /usuarios/{id} hacia Render
                            await API.delete(`/usuarios/${userId}`);
                            alert("Usuario eliminado con éxito.");
                            await cargarUsuariosAdmin(); // Recargamos la tabla para ver el cambio
                        } catch (err) {
                            alert(`Error al eliminar: ${err.message}`);
                        }
                    }
                });
            });

        } catch (error) {
            tbody.innerHTML = `<tr><td colspan="5" class="p-6 text-center text-red-500">❌ Error de autorización: ${error.message}</td></tr>`;
        }
    }

    // 5. INTERRUPTORES DE NAVEGACIÓN (Cambiar entre paneles de forma visual)
    btnNavDashboard.addEventListener("click", () => {
        // Estilos de botones activos/inactivos
        btnNavDashboard.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl bg-blue-800 text-white shadow-md shadow-blue-100 transition-all";
        btnNavAdmin.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl text-slate-500 hover:bg-slate-50 hover:text-slate-900 transition-all";
        
        panelUser.classList.remove("hidden");
        panelAdmin.classList.add("hidden");
        cargarContenido();
    });

    btnNavAdmin.addEventListener("click", () => {
        btnNavAdmin.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl bg-blue-800 text-white shadow-md shadow-blue-100 transition-all";
        btnNavDashboard.className = "w-full flex items-center gap-3 px-4 py-3 text-sm font-semibold rounded-2xl text-slate-500 hover:bg-slate-50 hover:text-slate-900 transition-all";
        
        panelAdmin.classList.remove("hidden");
        panelUser.classList.add("hidden");
        cargarContenido();
    });

    // 6. GESTIÓN DE CIERRE DE SESIÓN (Logout)
    btnLogout.addEventListener("click", () => {
        if (confirm("¿Deseas cerrar tu sesión en RingHere?")) {
            localStorage.clear(); // Limpiamos Token y Rol por completo del navegador
            window.location.href = "index.html"; // Al login de vuelta
        }
    });

    // 7. EJECUCIÓN INICIAL AL ENTRAR
    await cargarContenido();
});