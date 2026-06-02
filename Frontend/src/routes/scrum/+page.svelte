<script lang="ts">
    import axios from "axios";
    import { onMount } from "svelte";
    import { protegerRuta, logout, activarLogoutAutomatico, configurarAxios } from "$lib/auth";

    type CoreItem = {
        nombreSprint: string; nombreProyecto: string; porcentajeSimilitud: number;
        alertaRiesgo: string; horasNecesarias: number; horasDisponibles: number;
        deficit: number; freelancerAsignado: boolean; nombreFreelancer: string;
        horasAsignadasFreelancer: number; costoEstimadoFreelancer: number;
    };
    type Sprint   = { id:number; nombre:string; fechaInicio:string; fechaFin:string; horasEstimadas:number; estado:string; proyecto:{id:number;nombre:string}; };
    type Proyecto = { id:number; nombre:string; estado:string; };

    let nombre = $state("");
    let resultadosCore = $state<CoreItem[]>([]);
    let sprints = $state<Sprint[]>([]);
    let proyectos = $state<Proyecto[]>([]);

    let sprintNombre = $state("");
    let sprintFechaInicio = $state("");
    let sprintFechaFin = $state("");
    let sprintHoras = $state(0);
    let sprintEstado = $state("PLANIFICADO");
    let sprintProyectoId = $state("");
    let mensajeSprint = $state("");

    onMount(async () => {
        protegerRuta();
        configurarAxios();
        activarLogoutAutomatico();
        nombre = localStorage.getItem("nombre") || "";
        await cargarDatos();
    });

    async function cargarDatos() {
        const [core, s, p] = await Promise.all([
            axios.get("http://localhost:8080/api/core/analizar"),
            axios.get("http://localhost:8080/api/sprints"),
            axios.get("http://localhost:8080/api/proyectos")
        ]);
        resultadosCore = core.data;
        sprints = s.data;
        proyectos = p.data;
    }

    async function crearSprint() {
        await axios.post("http://localhost:8080/api/sprints", {
            nombre: sprintNombre, fechaInicio: sprintFechaInicio, fechaFin: sprintFechaFin,
            horasEstimadas: sprintHoras, estado: sprintEstado,
            proyecto: { id: Number(sprintProyectoId) }
        });
        mensajeSprint = "Sprint creado correctamente";
        sprintNombre = sprintFechaInicio = sprintFechaFin = sprintProyectoId = "";
        sprintHoras = 0;
        await cargarDatos();
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

        <section>
            <h2>Análisis del Motor Inteligente</h2>
            <table>
                <thead>
                    <tr>
                        <th>Sprint</th><th>Proyecto</th><th>Similitud</th><th>Riesgo</th>
                        <th>Horas necesarias</th><th>Horas disponibles</th><th>Déficit</th>
                        <th>Freelancer</th><th>Horas asignadas</th><th>Costo estimado</th>
                    </tr>
                </thead>
                <tbody>
                    {#each resultadosCore as item (item.nombreSprint)}
                    <tr>
                        <td>{item.nombreSprint}</td>
                        <td>{item.nombreProyecto}</td>
                        <td>{item.porcentajeSimilitud}%</td>
                        <td class="riesgo-{item.alertaRiesgo.toLowerCase()}">{item.alertaRiesgo}</td>
                        <td>{item.horasNecesarias}h</td>
                        <td>{item.horasDisponibles}h</td>
                        <td class="{item.deficit > 0 ? 'deficit-alto' : ''}">{item.deficit}h</td>
                        <td>{item.freelancerAsignado ? item.nombreFreelancer : '—'}</td>
                        <td>{item.horasAsignadasFreelancer > 0 ? item.horasAsignadasFreelancer + 'h' : '—'}</td>
                        <td>{item.costoEstimadoFreelancer > 0 ? '$' + item.costoEstimadoFreelancer.toFixed(2) : '—'}</td>
                    </tr>
                    {/each}
                </tbody>
            </table>
        </section>

        <section>
            <h2>Crear Sprint</h2>
            <div class="formulario">
                <input bind:value={sprintNombre}      placeholder="Nombre del sprint" />
                <input bind:value={sprintFechaInicio} type="date" />
                <input bind:value={sprintFechaFin}    type="date" />
                <input bind:value={sprintHoras}       type="number" placeholder="Horas estimadas" />
                <select bind:value={sprintEstado}>
                    <option value="PLANIFICADO">Planificado</option>
                    <option value="EN_CURSO">En curso</option>
                    <option value="COMPLETADO">Completado</option>
                </select>
                <select bind:value={sprintProyectoId}>
                    <option value="">Seleccionar proyecto</option>
                    {#each proyectos as p (p.id)}
                    <option value={p.id}>{p.nombre}</option>
                    {/each}
                </select>
                <button onclick={crearSprint}>Crear</button>
            </div>
            {#if mensajeSprint}<p class="ok">{mensajeSprint}</p>{/if}
        </section>

        <section>
            <h2>Sprints registrados</h2>
            <table>
                <thead>
                    <tr><th>Nombre</th><th>Proyecto</th><th>Inicio</th><th>Fin</th><th>Horas</th><th>Estado</th></tr>
                </thead>
                <tbody>
                    {#each sprints as s (s.id)}
                    <tr>
                        <td>{s.nombre}</td>
                        <td>{s.proyecto?.nombre ?? '—'}</td>
                        <td>{s.fechaInicio}</td>
                        <td>{s.fechaFin}</td>
                        <td>{s.horasEstimadas}h</td>
                        <td>{s.estado}</td>
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
    .riesgo-bajo  { color: #060; font-weight: bold; }
    .riesgo-medio { color: #850; font-weight: bold; }
    .riesgo-alto  { color: #c00; font-weight: bold; }
    .deficit-alto { color: #c00; font-weight: bold; }
    .ok { color: #060; font-size: 0.82rem; }
</style>