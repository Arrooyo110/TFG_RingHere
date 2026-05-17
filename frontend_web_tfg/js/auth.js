// js/auth.js

/**
 * FUNCIÓN AUXILIAR (Para tu defensa/memoria):
 * Decodifica la sección 'Payload' de un token JWT en Vanilla JS.
 * Un JWT está compuesto por tres partes separadas por puntos: Header.Payload.Signature
 */
function parseJwt(token) {
    try {
        const base64Url = token.split('.')[1]; // Extraemos la segunda parte (el payload)
        const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/'); // Convertimos Base64Url a Base64 estándar
        
        // Decodificamos el string protegiendo caracteres especiales y acentos
        const jsonPayload = decodeURIComponent(window.atob(base64).split('').map(function(c) {
            return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
        }).join(''));

        return JSON.parse(jsonPayload);
    } catch (e) {
        console.error("Error crítico decodificando el JWT:", e);
        return null;
    }
}

// Aseguramos que el script se ejecute cuando el HTML esté completamente cargado
document.addEventListener("DOMContentLoaded", () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const togglePasswordBtn = document.getElementById('togglePassword');

    // 1. LÓGICA DEL BOTÓN DEL OJO (MOSTRAR/OCULTAR CONTRASEÑA)
    if (togglePasswordBtn && passwordInput) {
        togglePasswordBtn.addEventListener('click', function () {
            const isPassword = passwordInput.getAttribute('type') === 'password';
            passwordInput.setAttribute('type', isPassword ? 'text' : 'password');
            
            // Feedback visual utilizando las clases de utilidad de Tailwind
            this.classList.toggle('text-blue-600');
            this.classList.toggle('text-slate-400');
        });
    }

    // 2. INTERCEPTOR DEL FORMULARIO DE LOGIN (CONEXIÓN CON EL BACKEND)
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault(); // Evitamos que la página se recargue por defecto

            const email = emailInput.value.trim();
            const password = passwordInput.value;

            // Buenas prácticas UX: Deshabilitamos el botón para evitar doble envío durante la carga
            const submitBtn = loginForm.querySelector("button[type='submit']");
            const originalText = submitBtn.innerText;
            submitBtn.disabled = true;
            submitBtn.innerText = "Verificando...";

            try {
                // Invocamos el método login de nuestro objeto central API (en js/api.js)
                const data = await API.login(email, password);
                
                // Si la API responde con éxito, almacenamos el token JWT en el LocalStorage
                localStorage.setItem("token", data.access_token);
                
                // Decodificamos las "claims" del token para extraer el rol del usuario
                const tokenData = parseJwt(data.access_token);
                if (tokenData && tokenData.role) {
                    localStorage.setItem("role", tokenData.role);
                } else {
                    localStorage.setItem("role", "user"); // Fallback por seguridad
                }

                // ¡Éxito total! Redirigimos al Dashboard único inteligente
                window.location.href = "dashboard.html";

            } catch (error) {
                // Captura el mensaje descriptivo que programamos en el manejo global de errores de FastAPI
                alert(`Error en el acceso: ${error.message}`);
            } finally {
                // Restauramos el estado del botón pase lo que pase
                submitBtn.disabled = false;
                submitBtn.innerText = originalText;
            }
        });
    }
});