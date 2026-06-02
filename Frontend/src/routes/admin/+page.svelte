<script lang="ts">
    import axios from "axios";
    import { onMount } from "svelte";
    import { protegerRuta, logout, activarLogoutAutomatico, configurarAxios } from "$lib/auth";

    let nombre = $state("");
    let usuarios = $state<{id:number,nombre:string,correo:string,rol:string,horasDisponibles:number}[]>([]);
    let proyectos = $state<{id:number,nombre:string,estado:string,descripcion:string}[]>([]);

    // Formulario nuevo usuario
    let nuevoNombre = $state("");
    let nuevoCorreo = $state("");
    let nuevoPassword = $state("");
    let nuevoRol = $state("ROLE_DEVELOPER");
    let nuevasHoras = $state(40);

    // Formulario nuevo proyecto
    let proyectoNombre = $state("");
    let proyectoEstado = $state("ACTIVO");
    let proyectoDescripcion = $state("");

    // Edición de usuario
    let editandoUsuarioId = $state<number | null>(null);
    let editNombre = $state("");
    let editCorreo = $state("");
    let editRol = $state("");
    let editHoras = $state(0);

    // Edición de proyecto
    let editandoProyectoId = $state<number | null>(null);
    let editProyectoNombre = $state("");
    let editProyectoEstado = $state("");
    let editProyectoDescripcion = $state("");

    let mensajeUsuario = $state("");
    let mensajeProyecto = $state("");

    onMount(async () => {
        protegerRuta();
        configurarAxios();
        activarLogoutAutomatico();
        nombre = localStorage.getItem("nombre") || "";
        await cargarDatos();
    });

    async function cargarDatos() {
        const [u, p] = await Promise.all([
            axios.get("http://localhost:8080/api/usuarios"),
            axios.get("http://localhost:8080/api/proyectos")
        ]);
        usuarios = u.data;
        proyectos = p.data;
    }

    // ── Usuarios ─────────────────────────────────────────
    async function crearUsuario() {
        await axios.post("http://localhost:8080/api/usuarios", {
            nombre: nuevoNombre, correo: nuevoCorreo, password: nuevoPassword,
            rol: nuevoRol, horasDisponibles: nuevasHoras
        });
        mensajeUsuario = "Usuario creado correctamente";
        nuevoNombre = nuevoCorreo = nuevoPassword = "";
        await cargarDatos();
    }

    function iniciarEdicionUsuario(u: {id:number,nombre:string,correo:string,rol:string,horasDisponibles:number}) {
        editandoUsuarioId = u.id;
        editNombre  = u.nombre;
        editCorreo  = u.correo;
        editRol     = u.rol;
        editHoras   = u.horasDisponibles;
    }

    async function guardarEdicionUsuario(id: number) {
        await axios.put(`http://localhost:8080/api/usuarios/${id}`, {
            nombre: editNombre, correo: editCorreo,
            rol: editRol, horasDisponibles: editHoras,
            password: ""
        });
        editandoUsuarioId = null;
        mensajeUsuario = "Usuario actualizado correctamente";
        await cargarDatos();
    }

    function cancelarEdicionUsuario() {
        editandoUsuarioId = null;
    }

    async function eliminarUsuario(id: number) {
        await axios.delete(`http://localhost:8080/api/usuarios/${id}`);
        await cargarDatos();
    }

    // ── Proyectos ─────────────────────────────────────────
    async function crearProyecto() {
        await axios.post("http://localhost:8080/api/proyectos", {
            nombre: proyectoNombre, estado: proyectoEstado, descripcion: proyectoDescripcion
        });
        mensajeProyecto = "Proyecto creado correctamente";
        proyectoNombre = proyectoDescripcion = "";
        await cargarDatos();
    }

    function iniciarEdicionProyecto(p: {id:number,nombre:string,estado:string,descripcion:string}) {
        editandoProyectoId       = p.id;
        editProyectoNombre       = p.nombre;
        editProyectoEstado       = p.estado;
        editProyectoDescripcion  = p.descripcion;
    }

    async function guardarEdicionProyecto(id: number) {
        await axios.put(`http://localhost:8080/api/proyectos/${id}`, {
            nombre: editProyectoNombre, estado: editProyectoEstado,
            descripcion: editProyectoDescripcion
        });
        editandoProyectoId = null;
        mensajeProyecto = "Proyecto actualizado correctamente";
        await cargarDatos();
    }

    function cancelarEdicionProyecto() {
        editandoProyectoId = null;
    }

    async function eliminarProyecto(id: number) {
        await axios.delete(`http://localhost:8080/api/proyectos/${id}`);
        await cargarDatos();
    }
</script>

<div class="layout">
    <aside>
        <p class="logo">ScrumCore</p>
        <p class="usuario">{nombre}</p>
        <p class="rol">Administrador</p>
        <button onclick={logout}>Cerrar sesión</button>
    </aside>

    <main>
        <h1>Panel de Administración</h1>

        <!-- USUARIOS -->
        <section>
            <h2>Usuarios</h2>

            <div class="formulario">
                <input bind:value={nuevoNombre}   placeholder="Nombre" />
                <input bind:value={nuevoCorreo}   placeholder="Correo" type="email" />
                <input bind:value={nuevoPassword} placeholder="Password" type="password" />
                <input bind:value={nuevasHoras}   placeholder="Horas" type="number" />
                <select bind:value={nuevoRol}>
                    <option value="ROLE_ADMIN">Admin</option>
                    <option value="ROLE_SCRUM_MASTER">Scrum Master</option>
                    <option value="ROLE_DEVELOPER">Developer</option>
                    <option value="ROLE_FREELANCER">Freelancer</option>
                </select>
                <button onclick={crearUsuario}>Agregar</button>
            </div>
            {#if mensajeUsuario}<p class="ok">{mensajeUsuario}</p>{/if}

            <table>
                <thead>
                    <tr>
                        <th>Nombre</th><th>Correo</th><th>Rol</th>
                        <th>Horas</th><th>Acciones</th>
                    </tr>
                </thead>
                <tbody>
                    {#each usuarios as u (u.id)}
                    <tr>
                        {#if editandoUsuarioId === u.id}
                            <!-- FILA EN MODO EDICIÓN -->
                            <td><input bind:value={editNombre} /></td>
                            <td><input bind:value={editCorreo} /></td>
                            <td>
                                <select bind:value={editRol}>
                                    <option value="ROLE_ADMIN">Admin</option>
                                    <option value="ROLE_SCRUM_MASTER">Scrum Master</option>
                                    <option value="ROLE_DEVELOPER">Developer</option>
                                    <option value="ROLE_FREELANCER">Freelancer</option>
                                </select>
                            </td>
                            <td><input bind:value={editHoras} type="number" style="width:60px" /></td>
                            <td>
                                <button onclick={() => guardarEdicionUsuario(u.id)}>Guardar</button>
                                <button class="btn-cancelar" onclick={cancelarEdicionUsuario}>Cancelar</button>
                            </td>
                        {:else}
                            <!-- FILA NORMAL -->
                            <td>{u.nombre}</td>
                            <td>{u.correo}</td>
                            <td>{u.rol}</td>
                            <td>{u.horasDisponibles}h</td>
                            <td>
                                <button class="btn-editar" onclick={() => iniciarEdicionUsuario(u)}>Editar</button>
                                <button class="btn-eliminar" onclick={() => eliminarUsuario(u.id)}>Eliminar</button>
                            </td>
                        {/if}
                    </tr>
                    {/each}
                </tbody>
            </table>
        </section>

        <!-- PROYECTOS -->
        <section>
            <h2>Proyectos</h2>

            <div class="formulario">
                <input bind:value={proyectoNombre}      placeholder="Nombre del proyecto" />
                <input bind:value={proyectoDescripcion} placeholder="Descripción" />
                <select bind:value={proyectoEstado}>
                    <option value="ACTIVO">Activo</option>
                    <option value="HISTORICO">Histórico</option>
                </select>
                <button onclick={crearProyecto}>Agregar</button>
            </div>
            {#if mensajeProyecto}<p class="ok">{mensajeProyecto}</p>{/if}

            <table>
                <thead>
                    <tr><th>Nombre</th><th>Estado</th><th>Descripción</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    {#each proyectos as p (p.id)}
                    <tr>
                        {#if editandoProyectoId === p.id}
                            <!-- FILA EN MODO EDICIÓN -->
                            <td><input bind:value={editProyectoNombre} /></td>
                            <td>
                                <select bind:value={editProyectoEstado}>
                                    <option value="ACTIVO">Activo</option>
                                    <option value="HISTORICO">Histórico</option>
                                </select>
                            </td>
                            <td><input bind:value={editProyectoDescripcion} /></td>
                            <td>
                                <button onclick={() => guardarEdicionProyecto(p.id)}>Guardar</button>
                                <button class="btn-cancelar" onclick={cancelarEdicionProyecto}>Cancelar</button>
                            </td>
                        {:else}
                            <!-- FILA NORMAL -->
                            <td>{p.nombre}</td>
                            <td>{p.estado}</td>
                            <td>{p.descripcion}</td>
                            <td>
                                <button class="btn-editar" onclick={() => iniciarEdicionProyecto(p)}>Editar</button>
                                <button class="btn-eliminar" onclick={() => eliminarProyecto(p.id)}>Eliminar</button>
                            </td>
                        {/if}
                    </tr>
                    {/each}
                </tbody>
            </table>
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
    .formulario { display: flex; gap: 0.5rem; flex-wrap: wrap; margin-bottom: 0.75rem; }
    input, select { padding: 0.4rem 0.6rem; border: 1px solid #ccc; font-size: 0.88rem; }
    button { padding: 0.4rem 0.8rem; background: #222; color: white; border: none; cursor: pointer; font-size: 0.88rem; }
    button:hover { background: #444; }
    table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
    th { text-align: left; padding: 0.5rem; background: #f0f0f0; border-bottom: 1px solid #ddd; }
    td { padding: 0.5rem; border-bottom: 1px solid #eee; }
    td input { width: 100%; box-sizing: border-box; }
    .btn-editar { background: #555; font-size: 0.8rem; padding: 0.2rem 0.5rem; margin-right: 0.3rem; }
    .btn-editar:hover { background: #333; }
    .btn-eliminar { background: #c00; font-size: 0.8rem; padding: 0.2rem 0.5rem; }
    .btn-eliminar:hover { background: #a00; }
    .btn-cancelar { background: #888; font-size: 0.8rem; padding: 0.2rem 0.5rem; margin-left: 0.3rem; }
    .btn-cancelar:hover { background: #666; }
    .ok { color: #060; font-size: 0.82rem; }
</style>