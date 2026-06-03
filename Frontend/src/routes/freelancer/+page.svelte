<script lang="ts">
    import axios from "axios";
    import { onMount } from "svelte";
    import { protegerRuta, logout, activarLogoutAutomatico, configurarAxios } from "$lib/auth";

    type Tarea = {
        id: number; nombre: string; horasNecesarias: number; estado: string;
        sprint: { id: number; nombre: string };
        usuarioAsignado: { id: number; nombre: string } | null;
    };

    type Perfil = {
        id: number; especialidad: string;
        horasDisponibles: number; costoHora: number; activo: boolean;
        usuario: { id: number; nombre: string; correo: string };
    };

    let nombre = $state("");
    let tareas = $state<Tarea[]>([]);
    let perfil = $state<Perfil | null>(null);
    let usuarioId = $state<number | null>(null);
    let mensajeEstado = $state("");

    onMount(async () => {
        protegerRuta();
        configurarAxios();
        activarLogoutAutomatico();
        nombre = localStorage.getItem("nombre") || "";
        await cargarDatos();
    });

    async function cargarDatos() {
        const token = localStorage.getItem("token");
        if (!token) return;

        // Extraer correo del token JWT
        const payload = JSON.parse(atob(token.split(".")[1]));
        const correo  = payload.sub;

        // Buscar usuario por correo
        const resUsuarios = await axios.get("http://localhost:8080/api/usuarios");
        const usuario = resUsuarios.data.find(
            (u: { correo: string; id: number }) => u.correo === correo
        );

        if (!usuario) return;
        usuarioId = usuario.id;

        // Cargar perfil freelancer y tareas en paralelo
        const [resPerfil, resTareas] = await Promise.all([
            axios.get(`http://localhost:8080/api/freelancers/usuario/${usuario.id}`),
            axios.get(`http://localhost:8080/api/tareas/usuario/${usuario.id}`)
        ]);

        perfil = resPerfil.data;
        tareas = resTareas.data;
    }

    async function cambiarEstado(id: number, nuevoEstado: string) {
        mensajeEstado = "";
        await axios.put(`http://localhost:8080/api/tareas/${id}`, {
            estado: nuevoEstado
        });
        mensajeEstado = "Estado actualizado correctamente";
        // Recargar solo las tareas
        if (usuarioId) {
            const res = await axios.get(`http://localhost:8080/api/tareas/usuario/${usuarioId}`);
            tareas = res.data;
        }
    }
</script>

<div class="layout">
    <aside>
        <p class="logo">ScrumCore</p>
        <p class="usuario">{nombre}</p>
        <p class="rol">Freelancer</p>
        <button onclick={logout}>Cerrar sesión</button>
    </aside>

    <main>
        <h1>Panel Freelancer</h1>

        <!-- MI PERFIL -->
        {#if perfil}
        <section>
            <h2>Mi perfil</h2>
            <table>
                <thead>
                    <tr>
                        <th>Especialidad</th>
                        <th>Horas disponibles</th>
                        <th>Tarifa por hora</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    <tr>
                        <td>{perfil.especialidad}</td>
                        <td>{perfil.horasDisponibles}h</td>
                        <td>${perfil.costoHora}/h</td>
                        <td>{perfil.activo ? 'Activo' : 'Inactivo'}</td>
                    </tr>
                </tbody>
            </table>
        </section>
        {/if}

        <!-- TAREAS ASIGNADAS -->
        <section>
            <h2>Mis tareas</h2>
            {#if mensajeEstado}<p class="ok">{mensajeEstado}</p>{/if}

            {#if tareas.length === 0}
                <p class="vacio">No tienes tareas asignadas aún.</p>
            {:else}
                <table>
                    <thead>
                        <tr>
                            <th>Tarea</th>
                            <th>Sprint</th>
                            <th>Horas</th>
                            <th>Estado actual</th>
                            <th>Cambiar estado</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each tareas as t (t.id)}
                        <tr>
                            <td>{t.nombre}</td>
                            <td>{t.sprint?.nombre ?? '—'}</td>
                            <td>{t.horasNecesarias}h</td>
                            <td>
                                <span class="estado-{t.estado.toLowerCase().replace('_', '-')}">
                                    {t.estado}
                                </span>
                            </td>
                            <td>
                                <select
                                    onchange={(e) => cambiarEstado(t.id, (e.target as HTMLSelectElement).value)}>
                                    <option value="PENDIENTE"   selected={t.estado === 'PENDIENTE'}>Pendiente</option>
                                    <option value="EN_PROGRESO" selected={t.estado === 'EN_PROGRESO'}>En progreso</option>
                                    <option value="COMPLETADA"  selected={t.estado === 'COMPLETADA'}>Completada</option>
                                </select>
                            </td>
                        </tr>
                        {/each}
                    </tbody>
                </table>
            {/if}
        </section>
    </main>
</div>

<style>
    :global(body) { margin: 0; font-family: Arial, sans-serif; background: #f5f5f5; }
    .layout { display: flex; min-height: 100vh; }
    aside { width: 180px; background: #222; color: white; padding: 1.5rem 1rem; display: flex; flex-direction: column; gap: 0.5rem; }
    .logo { font-weight: bold; font-size: 1.1rem; margin: 0 0 1rem; }
    .usuario { font-size: 0.85rem; margin: 0; }
    .rol { font-size: 0.75rem; color: #aaa; margin: 0 0 1rem; }
    aside button { margin-top: auto; padding: 0.4rem; background: #555; color: white; border: none; cursor: pointer; font-size: 0.85rem; }
    aside button:hover { background: #777; }
    main { flex: 1; padding: 2rem; }
    h1 { font-size: 1.3rem; margin: 0 0 1.5rem; }
    section { background: white; border: 1px solid #ddd; padding: 1.25rem; margin-bottom: 1.5rem; }
    h2 { font-size: 1rem; margin: 0 0 1rem; border-bottom: 1px solid #eee; padding-bottom: 0.5rem; }
    table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
    th { text-align: left; padding: 0.5rem; background: #f0f0f0; border-bottom: 1px solid #ddd; }
    td { padding: 0.5rem; border-bottom: 1px solid #eee; }
    select { padding: 0.3rem; border: 1px solid #ccc; font-size: 0.85rem; }
    .estado-pendiente   { color: #888; }
    .estado-en-progreso { color: #850; font-weight: bold; }
    .estado-completada  { color: #060; font-weight: bold; }
    .vacio { color: #888; font-size: 0.88rem; }
    .ok { color: #060; font-size: 0.82rem; margin: 0 0 0.75rem; }
</style>