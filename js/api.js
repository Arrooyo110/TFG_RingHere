// js/api.js

// URL pública de tu backend en Render
const BASE_URL = "https://ringhere-api.onrender.com";

/**
 * Función auxiliar para generar las cabeceras HTTP necesarias.
 * Inyecta automáticamente el token JWT si está guardado en el navegador.
 */
function getHeaders() {
    const token = localStorage.getItem("token");
    const headers = {
        "Content-Type": "application/json"
    };
    
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
}

/**
 * Manejador centralizado de respuestas y errores de la API.
 */
async function handleResponse(response) {
    if (!response.ok) {
        let errorMessage = "Ocurrió un error inesperado";
        try {
            // Intentamos extraer el mensaje de error exacto ('detail') que envía FastAPI
            const errorData = await response.json();
            errorMessage = errorData.detail || errorMessage;
        } catch (e) {
            errorMessage = response.statusText || errorMessage;
        }

        // CONTROL DE ACCESO GLOBAL: Si el servidor dice que el token expiró (401)
        if (response.status === 401) {
            localStorage.clear(); // Limpiamos datos corruptos o antiguos
            // Si no estamos ya en el login, redirigimos al usuario para que vuelva a autenticarse
            if (!window.location.pathname.endsWith("index.html") && window.location.pathname !== "/") {
                window.location.href = "index.html";
            }
        }

        throw new Error(errorMessage);
    }

    // Si el servidor responde con un 204 (No Content, típico de los DELETE), devolvemos objeto vacío
    if (response.status === 204) return {};

    return await response.json();
}

/**
 * OBJETO CENTRAL DE CONEXIÓN (API)
 * Expone los métodos HTTP estandarizados para consumir tus endpoints.
 */
const API = {
    // Petición GET
    async get(endpoint) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: "GET",
                headers: getHeaders()
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`[API GET ERROR] en ${endpoint}:`, error.message);
            throw error;
        }
    },

    // Petición POST Estándar en JSON
    async post(endpoint, data) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: "POST",
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`[API POST ERROR] en ${endpoint}:`, error.message);
            throw error;
        }
    },

    // Petición POST Especial para el Login
    // Explicación TFG: OAuth2 en FastAPI requiere formato x-www-form-urlencoded, no JSON plano.
    async login(username, password) {
        try {
            const formData = new URLSearchParams();
            formData.append("username", username);
            formData.append("password", password);

            const response = await fetch(`${BASE_URL}/login`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                    
                },
                body: formData
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`[API LOGIN ERROR]:`, error.message);
            throw error;
        }
    },

    // Petición PUT
    async put(endpoint, data) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: "PUT",
                headers: getHeaders(),
                body: JSON.stringify(data)
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`[API PUT ERROR] en ${endpoint}:`, error.message);
            throw error;
        }
    },

    // Petición DELETE
    async delete(endpoint) {
        try {
            const response = await fetch(`${BASE_URL}${endpoint}`, {
                method: "DELETE",
                headers: getHeaders()
            });
            return await handleResponse(response);
        } catch (error) {
            console.error(`[API DELETE ERROR] en ${endpoint}:`, error.message);
            throw error;
        }
    }
};