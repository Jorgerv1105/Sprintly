<script lang="ts">
    import axios from "axios";
    import { onMount } from "svelte";
    import { protegerRuta, logout, activarLogoutAutomatico, configurarAxios } from "$lib/auth";

    type DetalleHistorico = {
        proyectoNombre: string; sprintNombre: string;
        puntajeObtenido: number; reglasAplicadas: string[];
    };
    type EquipoInterno = { nombre: string; rol: string; horasDisponibles: number };
    type CoreItem = {
        nombreSprint: string; nombreProyecto: string;
        porcentajeSimilitud: number; alertaRiesgo: string;
        razonSimilitud: string; detalleHistorico: DetalleHistorico[];
        horasNecesarias: number; horasDisponibles: number;
        deficit: number; riesgoOperativo: number; viabilidad: string;
        equipoInterno: EquipoInterno[];
        freelancerAsignado: boolean; nombreFreelancer: string;
        especialidadFreelancer: string; horasAsignadasFreelancer: number;
        costoEstimadoFreelancer: number;
    };
    type Proyecto = { id: number; nombre: string; estado: string };
    type Sprint   = { id: number; nombre: string; fechaInicio: string; fechaFin: string; horasEstimadas: number; estado: string; proyecto: { id: number; nombre: string } };
    type Usuario  = { id: number; nombre: string; rol: string; horasDisponibles: number };
    type Tarea    = { id: number; nombre: string; horasNecesarias: number; estado: string; sprint: { id: number; nombre: string }; usuarioAsignado: { id: number; nombre: string } | null };

    let nombre     = $state("");
    let pestana    = $state<"motor" | "sprints" | "tareas">("motor");

    let proyectos  = $state<Proyecto[]>([]);
    let sprints    = $state<Sprint[]>([]);
    let developers = $state<Usuario[]>([]);
    let tareas     = $state<Tarea[]>([]);

    // Motor
    let sprintSeleccionado = $state("");
    let analisis = $state<CoreItem | null>(null);
    let analizando = $state(false);
    let errorAnalisis = $state("");

    // Formulario Sprint
    let sNombre = $state(""); let sInicio = $state(""); let sFin = $state("");
    let sHoras = $state(0);   let sEstado = $state("PLANIFICADO"); let sProyecto = $state("");
    let msgSprint = $state("");

    // Edición Sprint
    let editSprintId = $state<number|null>(null);
    let editSNombre = $state(""); let editSInicio = $state(""); let editSFin = $state("");
    let editSHoras = $state(0);  let editSEstado = $state(""); let editSProyecto = $state("");

    // Formulario Tarea
    let tNombre = $state(""); let tHoras = $state(0); let tEstado = $state("PENDIENTE");
    let tSprint = $state(""); let tDeveloper = $state(""); let msgTarea = $state("");

    // Edición Tarea
    let editTareaId = $state<number|null>(null);
    let editTNombre = $state(""); let editTHoras = $state(0); let editTEstado = $state("");
    let editTSprint = $state(""); let editTDeveloper = $state("");

    // Filtro tareas
    let filtroSprint = $state("");

    onMount(async () => {
        protegerRuta();
        configurarAxios();
        activarLogoutAutomatico();
        nombre = localStorage.getItem("nombre") || "";
        await cargarTodo();
    });

    async function cargarTodo() {
        const [p, s, u, t] = await Promise.all([
            axios.get("http://localhost:8080/api/proyectos"),
            axios.get("http://localhost:8080/api/sprints"),
            axios.get("http://localhost:8080/api/usuarios"),
            axios.get("http://localhost:8080/api/tareas")
        ]);
        proyectos  = p.data;
        sprints    = s.data;
        developers = u.data.filter((u: Usuario) => u.rol === "ROLE_DEVELOPER");
        tareas     = t.data;
    }

    // ── Motor: analiza en tiempo real al seleccionar sprint ──
    async function analizarSprint() {
        if (!sprintSeleccionado) return;
        analizando = true;
        errorAnalisis = "";
        analisis = null;
        try {
            const res = await axios.get(
                `http://localhost:8080/api/core/analizar/${sprintSeleccionado}`
            );
            analisis = res.data;
        } catch (_e) {
            errorAnalisis = "Error al analizar el sprint";
        } finally {
            analizando = false;
        }
    }

    // ── Sprint CRUD ──────────────────────────────────────
    async function crearSprint() {
        if (!sNombre || !sInicio || !sFin || !sProyecto) return;
        await axios.post("http://localhost:8080/api/sprints", {
            nombre: sNombre, fechaInicio: sInicio, fechaFin: sFin,
            horasEstimadas: sHoras, estado: sEstado,
            proyecto: { id: Number(sProyecto) }
        });
        msgSprint = "Sprint creado correctamente";
        sNombre = sInicio = sFin = sProyecto = ""; sHoras = 0;
        await cargarTodo();
    }

    function iniciarEditSprint(s: Sprint) {
        editSprintId = s.id; editSNombre = s.nombre; editSInicio = s.fechaInicio;
        editSFin = s.fechaFin; editSHoras = s.horasEstimadas;
        editSEstado = s.estado; editSProyecto = String(s.proyecto?.id ?? "");
    }

    async function guardarEditSprint(id: number) {
        await axios.put(`http://localhost:8080/api/sprints/${id}`, {
            nombre: editSNombre, fechaInicio: editSInicio, fechaFin: editSFin,
            horasEstimadas: editSHoras, estado: editSEstado,
            proyecto: { id: Number(editSProyecto) }
        });
        editSprintId = null; msgSprint = "Sprint actualizado";
        await cargarTodo();
    }

    async function eliminarSprint(id: number) {
        if (!confirm("¿Eliminar este sprint?")) return;
        await axios.delete(`http://localhost:8080/api/sprints/${id}`);
        await cargarTodo();
    }

    // ── Tarea CRUD ───────────────────────────────────────
    async function crearTarea() {
        if (!tNombre || !tSprint) return;
        await axios.post("http://localhost:8080/api/tareas", {
            nombre: tNombre, horasNecesarias: tHoras, estado: tEstado,
            sprint: { id: Number(tSprint) },
            usuarioAsignado: tDeveloper ? { id: Number(tDeveloper) } : null
        });
        msgTarea = "Tarea creada correctamente";
        tNombre = tSprint = tDeveloper = ""; tHoras = 0;
        await cargarTodo();
    }

    function iniciarEditTarea(t: Tarea) {
        editTareaId = t.id; editTNombre = t.nombre; editTHoras = t.horasNecesarias;
        editTEstado = t.estado; editTSprint = String(t.sprint?.id ?? "");
        editTDeveloper = String(t.usuarioAsignado?.id ?? "");
    }

    async function guardarEditTarea(id: number) {
        await axios.put(`http://localhost:8080/api/tareas/${id}`, {
            nombre: editTNombre, horasNecesarias: editTHoras, estado: editTEstado,
            sprint: { id: Number(editTSprint) },
            usuarioAsignado: editTDeveloper ? { id: Number(editTDeveloper) } : null
        });
        editTareaId = null; msgTarea = "Tarea actualizada";
        await cargarTodo();
    }

    async function eliminarTarea(id: number) {
        await axios.delete(`http://localhost:8080/api/tareas/${id}`);
        await cargarTodo();
    }

    function tareasFiltradas() {
        if (!filtroSprint) return tareas;
        return tareas.filter(t => String(t.sprint?.id) === filtroSprint);
    }
</script>

<div class="layout">
    <aside>
        <p class="logo">ScrumCore</p>
        <p class="usuario">{nombre}</p>
        <p class="rol">Scrum Master</p>
        <button onclick={logout}>Cerrar sesión</button>
    </aside>

    <main>
        <h1>Panel Scrum Master</h1>

        <nav>
            <button class={pestana === 'motor'   ? 'activo' : ''} onclick={() => pestana = 'motor'}>Motor de Análisis</button>
            <button class={pestana === 'sprints' ? 'activo' : ''} onclick={() => pestana = 'sprints'}>Sprints</button>
            <button class={pestana === 'tareas'  ? 'activo' : ''} onclick={() => pestana = 'tareas'}>Tareas</button>
        </nav>

        <!-- ── MOTOR ────────────────────────────────────── -->
        {#if pestana === 'motor'}

        <section>
            <h2>Seleccionar sprint a analizar</h2>
            <div class="fila-analisis">
                <select bind:value={sprintSeleccionado} onchange={analizarSprint}>
                    <option value="">— Selecciona un sprint —</option>
                    {#each sprints as s (s.id)}
                    <option value={s.id}>
                        {s.nombre} — {s.proyecto?.nombre} ({s.estado})
                    </option>
                    {/each}
                </select>
                {#if analizando}<span class="cargando">Analizando...</span>{/if}
            </div>
            {#if errorAnalisis}<p class="error">{errorAnalisis}</p>{/if}
        </section>

        {#if analisis}
        <section>
            <h2>{analisis.nombreSprint} — {analisis.nombreProyecto}</h2>

            <!-- KPIs -->
            <div class="kpis">
                <div class="kpi">
                    <span class="kpi-label">Similitud histórica</span>
                    <span class="kpi-valor">{analisis.porcentajeSimilitud}%</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label">Alerta de riesgo</span>
                    <span class="kpi-valor riesgo-{analisis.alertaRiesgo.toLowerCase()}">{analisis.alertaRiesgo}</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label">Horas necesarias</span>
                    <span class="kpi-valor">{analisis.horasNecesarias}h</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label">Horas disponibles</span>
                    <span class="kpi-valor">{analisis.horasDisponibles}h</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label">Déficit</span>
                    <span class="kpi-valor {analisis.deficit > 0 ? 'rojo' : 'verde'}">{analisis.deficit}h</span>
                </div>
                <div class="kpi">
                    <span class="kpi-label">Viabilidad</span>
                    <span class="kpi-valor {analisis.viabilidad === 'DEFICITARIO' ? 'rojo' : 'verde'}">{analisis.viabilidad}</span>
                </div>
            </div>

            <!-- Razón -->
            {#if analisis.razonSimilitud}
            <div class="razon">
                <strong>Razón de similitud:</strong> {analisis.razonSimilitud}
            </div>
            {/if}

            <!-- Comparación histórica -->
            <h3>Proyectos históricos comparados</h3>
            {#if analisis.detalleHistorico.length === 0}
                <p class="vacio">No hay proyectos históricos para comparar. Crea proyectos con estado HISTORICO.</p>
            {:else}
                <table>
                    <thead>
                        <tr>
                            <th>Proyecto histórico</th>
                            <th>Sprint comparado</th>
                            <th>Puntaje</th>
                            <th>Reglas que aplicaron</th>
                        </tr>
                    </thead>
                    <tbody>
                        {#each analisis.detalleHistorico.sort((a, b) => b.puntajeObtenido - a.puntajeObtenido) as d (d.sprintNombre)}
                        <tr class="{d.puntajeObtenido === analisis.porcentajeSimilitud ? 'fila-mejor' : ''}">
                            <td>{d.proyectoNombre}</td>
                            <td>{d.sprintNombre}</td>
                            <td><strong>{d.puntajeObtenido}pts</strong></td>
                            <td>
                                {#if d.reglasAplicadas.length === 0}
                                    <span class="sin-regla">Ninguna regla aplicó</span>
                                {:else}
                                    {#each d.reglasAplicadas as r}
                                        <span class="regla">{r}</span>
                                    {/each}
                                {/if}
                            </td>
                        </tr>
                        {/each}
                    </tbody>
                </table>
            {/if}

            <!-- Equipo y freelancer -->
            <div class="doble">
                <div>
                    <h3>Equipo disponible</h3>
                    <table>
                        <thead>
                            <tr><th>Developer</th><th>Horas disponibles</th></tr>
                        </thead>
                        <tbody>
                            {#each analisis.equipoInterno as e (e.nombre)}
                            <tr>
                                <td>{e.nombre}</td>
                                <td>{e.horasDisponibles}h</td>
                            </tr>
                            {/each}
                            <tr class="fila-total">
                                <td><strong>Total</strong></td>
                                <td><strong>{analisis.horasDisponibles}h</strong></td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div>
                    <h3>Resultado del análisis de carga</h3>
                    <table>
                        <tbody>
                            <tr><td>Horas de tareas</td><td><strong>{analisis.horasNecesarias}h</strong></td></tr>
                            <tr><td>Capacidad del equipo</td><td><strong>{analisis.horasDisponibles}h</strong></td></tr>
                            <tr><td>Déficit</td><td class="{analisis.deficit > 0 ? 'rojo' : 'verde'}"><strong>{analisis.deficit}h</strong></td></tr>
                            <tr><td>Riesgo operativo</td><td class="{analisis.riesgoOperativo > 30 ? 'rojo' : 'verde'}"><strong>{analisis.riesgoOperativo}%</strong></td></tr>
                        </tbody>
                    </table>

                    {#if analisis.freelancerAsignado}
                    <div class="freelancer-box">
                        <strong>Freelancer sugerido automáticamente:</strong><br/>
                        {analisis.nombreFreelancer} — {analisis.especialidadFreelancer}<br/>
                        Horas a cubrir: {analisis.horasAsignadasFreelancer}h
                        — Costo estimado: <strong>${analisis.costoEstimadoFreelancer.toFixed(2)}</strong>
                    </div>
                    {/if}
                </div>
            </div>

        </section>
        {/if}

        {/if}

        <!-- ── SPRINTS ───────────────────────────────────── -->
        {#if pestana === 'sprints'}
        <section>
            <h2>Crear Sprint</h2>
            <div class="formulario">
                <div class="campo">
                    <label>Nombre</label>
                    <input bind:value={sNombre} placeholder="Nombre del sprint" />
                </div>
                <div class="campo">
                    <label>Proyecto</label>
                    <select bind:value={sProyecto}>
                        <option value="">Seleccionar proyecto</option>
                        {#each proyectos as p (p.id)}
                        <option value={p.id}>{p.nombre} — {p.estado}</option>
                        {/each}
                    </select>
                </div>
                <div class="campo">
                    <label>Estado</label>
                    <select bind:value={sEstado}>
                        <option value="PLANIFICADO">Planificado</option>
                        <option value="EN_CURSO">En curso</option>
                        <option value="COMPLETADO">Completado</option>
                    </select>
                </div>
                <div class="campo">
                    <label>Fecha inicio</label>
                    <input bind:value={sInicio} type="date" />
                </div>
                <div class="campo">
                    <label>Fecha fin</label>
                    <input bind:value={sFin} type="date" />
                </div>
                <div class="campo">
                    <label>Horas estimadas</label>
                    <input bind:value={sHoras} type="number" />
                </div>
            </div>
            <button onclick={crearSprint}>Crear Sprint</button>
            {#if msgSprint}<p class="ok">{msgSprint}</p>{/if}
        </section>

        <section>
            <h2>Sprints registrados</h2>
            <table>
                <thead>
                    <tr><th>Nombre</th><th>Proyecto</th><th>Estado</th><th>Inicio</th><th>Fin</th><th>Horas</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    {#each sprints as s (s.id)}
                    <tr>
                        {#if editSprintId === s.id}
                            <td><input bind:value={editSNombre} /></td>
                            <td>
                                <select bind:value={editSProyecto}>
                                    {#each proyectos as p (p.id)}
                                    <option value={p.id}>{p.nombre}</option>
                                    {/each}
                                </select>
                            </td>
                            <td>
                                <select bind:value={editSEstado}>
                                    <option value="PLANIFICADO">Planificado</option>
                                    <option value="EN_CURSO">En curso</option>
                                    <option value="COMPLETADO">Completado</option>
                                </select>
                            </td>
                            <td><input bind:value={editSInicio} type="date" /></td>
                            <td><input bind:value={editSFin}    type="date" /></td>
                            <td><input bind:value={editSHoras}  type="number" style="width:70px" /></td>
                            <td>
                                <button onclick={() => guardarEditSprint(s.id)}>Guardar</button>
                                <button class="btn-cancelar" onclick={() => editSprintId = null}>Cancelar</button>
                            </td>
                        {:else}
                            <td>{s.nombre}</td>
                            <td>{s.proyecto?.nombre ?? '—'}</td>
                            <td>{s.estado}</td>
                            <td>{s.fechaInicio}</td>
                            <td>{s.fechaFin}</td>
                            <td>{s.horasEstimadas}h</td>
                            <td>
                                <button class="btn-editar" onclick={() => iniciarEditSprint(s)}>Editar</button>
                                <button class="btn-eliminar" onclick={() => eliminarSprint(s.id)}>Eliminar</button>
                            </td>
                        {/if}
                    </tr>
                    {/each}
                </tbody>
            </table>
        </section>
        {/if}

        <!-- ── TAREAS ────────────────────────────────────── -->
        {#if pestana === 'tareas'}
        <section>
            <h2>Asignar nueva tarea</h2>
            <div class="formulario">
                <div class="campo">
                    <label>Nombre de la tarea</label>
                    <input bind:value={tNombre} placeholder="Descripción" />
                </div>
                <div class="campo">
                    <label>Sprint</label>
                    <select bind:value={tSprint}>
                        <option value="">Seleccionar sprint</option>
                        {#each sprints as s (s.id)}
                        <option value={s.id}>{s.nombre} — {s.proyecto?.nombre}</option>
                        {/each}
                    </select>
                </div>
                <div class="campo">
                    <label>Developer asignado</label>
                    <select bind:value={tDeveloper}>
                        <option value="">Sin asignar</option>
                        {#each developers as d (d.id)}
                        <option value={d.id}>{d.nombre} ({d.horasDisponibles}h)</option>
                        {/each}
                    </select>
                </div>
                <div class="campo">
                    <label>Estado</label>
                    <select bind:value={tEstado}>
                        <option value="PENDIENTE">Pendiente</option>
                        <option value="EN_PROGRESO">En progreso</option>
                        <option value="COMPLETADA">Completada</option>
                    </select>
                </div>
                <div class="campo">
                    <label>Horas necesarias</label>
                    <input bind:value={tHoras} type="number" />
                </div>
            </div>
            <button onclick={crearTarea}>Asignar tarea</button>
            {#if msgTarea}<p class="ok">{msgTarea}</p>{/if}
        </section>

        <section>
            <h2>Tareas registradas</h2>
            <div class="filtro">
                <label>Filtrar por sprint:</label>
                <select bind:value={filtroSprint}>
                    <option value="">Todos los sprints</option>
                    {#each sprints as s (s.id)}
                    <option value={s.id}>{s.nombre}</option>
                    {/each}
                </select>
            </div>

            <table>
                <thead>
                    <tr><th>Tarea</th><th>Sprint</th><th>Developer</th><th>Horas</th><th>Estado</th><th>Acciones</th></tr>
                </thead>
                <tbody>
                    {#each tareasFiltradas() as t (t.id)}
                    <tr>
                        {#if editTareaId === t.id}
                            <td><input bind:value={editTNombre} /></td>
                            <td>
                                <select bind:value={editTSprint}>
                                    {#each sprints as s (s.id)}
                                    <option value={s.id}>{s.nombre}</option>
                                    {/each}
                                </select>
                            </td>
                            <td>
                                <select bind:value={editTDeveloper}>
                                    <option value="">Sin asignar</option>
                                    {#each developers as d (d.id)}
                                    <option value={d.id}>{d.nombre}</option>
                                    {/each}
                                </select>
                            </td>
                            <td><input bind:value={editTHoras} type="number" style="width:60px" /></td>
                            <td>
                                <select bind:value={editTEstado}>
                                    <option value="PENDIENTE">Pendiente</option>
                                    <option value="EN_PROGRESO">En progreso</option>
                                    <option value="COMPLETADA">Completada</option>
                                </select>
                            </td>
                            <td>
                                <button onclick={() => guardarEditTarea(t.id)}>Guardar</button>
                                <button class="btn-cancelar" onclick={() => editTareaId = null}>Cancelar</button>
                            </td>
                        {:else}
                            <td>{t.nombre}</td>
                            <td>{t.sprint?.nombre ?? '—'}</td>
                            <td>{t.usuarioAsignado?.nombre ?? 'Sin asignar'}</td>
                            <td>{t.horasNecesarias}h</td>
                            <td>{t.estado}</td>
                            <td>
                                <button class="btn-editar" onclick={() => iniciarEditTarea(t)}>Editar</button>
                                <button class="btn-eliminar" onclick={() => eliminarTarea(t.id)}>Eliminar</button>
                            </td>
                        {/if}
                    </tr>
                    {/each}
                </tbody>
            </table>
        </section>
        {/if}

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
    h1 { font-size: 1.3rem; margin: 0 0 1rem; }
    h3 { font-size: 0.9rem; margin: 1rem 0 0.5rem; color: #333; }

    nav { display: flex; margin-bottom: 1.5rem; border-bottom: 2px solid #222; }
    nav button { padding: 0.5rem 1.25rem; background: transparent; color: #555; border: none; border-bottom: 3px solid transparent; cursor: pointer; font-size: 0.9rem; margin-bottom: -2px; }
    nav button:hover { color: #222; }
    nav button.activo { color: #222; font-weight: bold; border-bottom: 3px solid #222; background: transparent; }

    section { background: white; border: 1px solid #ddd; padding: 1.25rem; margin-bottom: 1.25rem; }
    h2 { font-size: 1rem; margin: 0 0 0.75rem; border-bottom: 1px solid #eee; padding-bottom: 0.4rem; }

    /* Motor */
    .fila-analisis { display: flex; align-items: center; gap: 1rem; }
    .cargando { font-size: 0.82rem; color: #666; }

    .kpis { display: grid; grid-template-columns: repeat(6,1fr); border: 1px solid #eee; margin-bottom: 1rem; }
    .kpi { padding: 0.6rem 0.75rem; border-right: 1px solid #eee; }
    .kpi:last-child { border-right: none; }
    .kpi-label { display: block; font-size: 0.7rem; color: #888; margin-bottom: 0.15rem; }
    .kpi-valor { display: block; font-size: 1rem; font-weight: bold; }

    .razon { font-size: 0.82rem; color: #555; padding: 0.5rem 0.75rem; background: #fafafa; border: 1px solid #eee; margin-bottom: 0.75rem; }

    .doble { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; margin-top: 1rem; }

    .fila-mejor { background: #f0f7ff; }
    .fila-mejor td { font-weight: bold; }

    .regla { display: inline-block; font-size: 0.75rem; background: #f0f0f0; padding: 0.1rem 0.4rem; margin: 0.1rem; border: 1px solid #ddd; }
    .sin-regla { font-size: 0.78rem; color: #aaa; }
    .fila-total td { background: #f7f7f7; }

    .freelancer-box { margin-top: 0.75rem; padding: 0.6rem 0.75rem; background: #fff8e1; border: 1px solid #ffe082; font-size: 0.82rem; color: #555; line-height: 1.7; }

    /* Formulario */
    .formulario { display: flex; flex-wrap: wrap; gap: 0.75rem; margin-bottom: 0.75rem; }
    .campo { display: flex; flex-direction: column; gap: 0.2rem; }
    .campo label { font-size: 0.78rem; color: #555; }
    .filtro { display: flex; align-items: center; gap: 0.5rem; margin-bottom: 0.75rem; font-size: 0.85rem; }

    input, select { padding: 0.4rem 0.6rem; border: 1px solid #ccc; font-size: 0.88rem; min-width: 150px; }
    input:focus, select:focus { outline: 1px solid #333; }

    button { padding: 0.4rem 0.9rem; background: #222; color: white; border: none; cursor: pointer; font-size: 0.88rem; }
    button:hover { background: #444; }
    .btn-editar { background: #555; margin-right: 0.3rem; }
    .btn-editar:hover { background: #333; }
    .btn-eliminar { background: #c00; }
    .btn-eliminar:hover { background: #a00; }
    .btn-cancelar { background: #888; margin-left: 0.3rem; }
    .btn-cancelar:hover { background: #666; }

    table { width: 100%; border-collapse: collapse; font-size: 0.88rem; }
    th { text-align: left; padding: 0.5rem; background: #f0f0f0; border-bottom: 1px solid #ddd; }
    td { padding: 0.5rem; border-bottom: 1px solid #eee; vertical-align: middle; }
    td input, td select { min-width: unset; width: 100%; box-sizing: border-box; }

    .riesgo-bajo  { color: #2e7d32; }
    .riesgo-medio { color: #f57f17; }
    .riesgo-alto  { color: #c62828; }
    .rojo  { color: #c00; }
    .verde { color: #2e7d32; }

    .ok    { color: #2e7d32; font-size: 0.82rem; margin: 0.5rem 0 0; }
    .error { color: #c00; font-size: 0.82rem; margin: 0; }
    .vacio { color: #aaa; font-size: 0.85rem; }
</style>