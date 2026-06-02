<script lang="ts">
    import axios from "axios";
    import { goto } from "$app/navigation";
    import { configurarAxios } from "$lib/auth";

    let correo = $state("");
    let password = $state("");
    let error = $state("");
    let mostrarRegistro = $state(false);
    let regNombre = $state("");
    let regCorreo = $state("");
    let regPassword = $state("");
    let mensajeRegistro = $state("");

    async function login() {
        error = "";
        try {
            const response = await axios.post("http://localhost:8080/auth/login", { correo, password });
            const data = response.data;
            localStorage.setItem("token", data.token);
            localStorage.setItem("rol", data.rol);
            localStorage.setItem("nombre", data.nombre);
            configurarAxios();
            if (data.rol === "ROLE_ADMIN")             await goto("/admin");
            else if (data.rol === "ROLE_SCRUM_MASTER") await goto("/scrum");
            else if (data.rol === "ROLE_DEVELOPER")    await goto("/developer");
            else if (data.rol === "ROLE_FREELANCER")   await goto("/freelancer");
            else await goto("/login");
        } catch (_e) {
            error = "Correo o contraseña incorrectos";
        }
    }

    async function registrar() {
        mensajeRegistro = "";
        try {
            await axios.post("http://localhost:8080/auth/register", {
                nombre: regNombre, correo: regCorreo, password: regPassword
            });
            mensajeRegistro = "Registro exitoso. Ahora puedes iniciar sesión.";
            regNombre = regCorreo = regPassword = "";
            mostrarRegistro = false;
        } catch (_e) {
            mensajeRegistro = "Error al registrar. El correo puede estar en uso.";
        }
    }
</script>

<div class="pagina">
    <div class="caja">
        <h2>ScrumCore</h2>
        <p class="sub">Sistema de Gestión de Sprints</p>

        {#if !mostrarRegistro}
            <label for="correo">Correo</label>
            <input id="correo" bind:value={correo} type="email" placeholder="correo@ejemplo.com" />

            <label for="pass">Contraseña</label>
            <input id="pass" bind:value={password} type="password" placeholder="••••••••" />

            {#if error}<p class="error">{error}</p>{/if}

            <button onclick={login}>Ingresar</button>
            <button class="btn-link" onclick={() => mostrarRegistro = true}>
                ¿No tienes cuenta? Regístrate
            </button>
        {:else}
            <label for="regnombre">Nombre</label>
            <input id="regnombre" bind:value={regNombre} type="text" placeholder="Tu nombre completo" />

            <label for="regcorreo">Correo</label>
            <input id="regcorreo" bind:value={regCorreo} type="email" placeholder="correo@ejemplo.com" />

            <label for="regpass">Contraseña</label>
            <input id="regpass" bind:value={regPassword} type="password" placeholder="••••••••" />

            {#if mensajeRegistro}<p class="ok">{mensajeRegistro}</p>{/if}

            <button onclick={registrar}>Crear cuenta</button>
            <button class="btn-link" onclick={() => mostrarRegistro = false}>
                ¿Ya tienes cuenta? Inicia sesión
            </button>
        {/if}
    </div>
</div>

<style>
    :global(body) { margin: 0; font-family: Arial, sans-serif; background: #f5f5f5; }
    .pagina { min-height: 100vh; display: flex; align-items: center; justify-content: center; }
    .caja { background: white; border: 1px solid #ddd; padding: 2rem; width: 320px; display: flex; flex-direction: column; gap: 0.5rem; }
    h2 { margin: 0 0 0.1rem; font-size: 1.4rem; }
    .sub { margin: 0 0 1rem; font-size: 0.85rem; color: #666; }
    label { font-size: 0.85rem; color: #333; }
    input { padding: 0.5rem; border: 1px solid #ccc; font-size: 0.95rem; width: 100%; box-sizing: border-box; }
    input:focus { outline: 1px solid #333; }
    button { margin-top: 0.25rem; padding: 0.6rem; background: #222; color: white; border: none; font-size: 0.9rem; cursor: pointer; }
    button:hover { background: #444; }
    .btn-link { background: transparent; color: #444; font-size: 0.82rem; text-decoration: underline; padding: 0.2rem; }
    .btn-link:hover { background: transparent; color: #000; }
    .error { color: #c00; font-size: 0.82rem; margin: 0; }
    .ok { color: #060; font-size: 0.82rem; margin: 0; }
</style>