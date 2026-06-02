<script lang="ts">
    import axios from "axios";
    import { onMount } from "svelte";
    import { protegerRuta } from "$lib/auth";

    type CoreItem = {
        proyectoNombre: string;
        horasProyecto: number;
        capacidadEquipo: number;
        deficit: number;
        freelancerAsignado: boolean;
    };

    let datos: CoreItem[] = [];

    onMount(async () => {

        // 🔐 PROTECCIÓN DE RUTA
        protegerRuta();

        // 🔑 TOKEN (opcional pero correcto)
        const token = localStorage.getItem("token");

        const res = await axios.get(
            "http://localhost:8080/core/analizar",
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        datos = res.data;
    });
</script>

<h1>Dashboard Core Scrum</h1>

<table border="1" cellpadding="10">
    <thead>
        <tr>
            <th>Proyecto</th>
            <th>Horas</th>
            <th>Capacidad</th>
            <th>Déficit</th>
            <th>Freelance</th>
        </tr>
    </thead>

    <tbody>
        {#each datos as item (item.proyectoNombre)}
            <tr>
                <td>{item.proyectoNombre}</td>
                <td>{item.horasProyecto}</td>
                <td>{item.capacidadEquipo}</td>
                <td>{item.deficit}</td>
                <td>{item.freelancerAsignado ? "SI" : "NO"}</td>
            </tr>
        {/each}
    </tbody>
</table>