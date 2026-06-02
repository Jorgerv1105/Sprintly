package com.scrumcore.service;

import com.scrumcore.dto.CoreResponseDTO;
import com.scrumcore.entity.Freelancer;
import com.scrumcore.entity.Proyecto;
import com.scrumcore.entity.Sprint;
import com.scrumcore.entity.Tarea;
import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.FreelancerRepository;
import com.scrumcore.repository.ProyectoRepository;
import com.scrumcore.repository.SprintRepository;
import com.scrumcore.repository.TareaRepository;
import com.scrumcore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CoreService {

    private final ProyectoRepository   proyectoRepository;
    private final SprintRepository     sprintRepository;
    private final TareaRepository      tareaRepository;
    private final UsuarioRepository    usuarioRepository;
    private final FreelancerRepository freelancerRepository;

    private static final int UMBRAL_DEFICIT_HORAS = 15;

    public CoreService(ProyectoRepository   proyectoRepository,
                       SprintRepository     sprintRepository,
                       TareaRepository      tareaRepository,
                       UsuarioRepository    usuarioRepository,
                       FreelancerRepository freelancerRepository) {
        this.proyectoRepository   = proyectoRepository;
        this.sprintRepository     = sprintRepository;
        this.tareaRepository      = tareaRepository;
        this.usuarioRepository    = usuarioRepository;
        this.freelancerRepository = freelancerRepository;
    }

    public List<CoreResponseDTO> analizarSprints() {

        List<CoreResponseDTO> resultados = new ArrayList<>();

        List<Proyecto>   proyectosActivos    = proyectoRepository.findByEstado("ACTIVO");
        List<Proyecto>   proyectosHistoricos = proyectoRepository.findByEstado("HISTORICO");

        // Ahora usamos la tabla freelancers directamente
        List<Freelancer> freelancers         = freelancerRepository.findByActivoTrue();

        // ── FOREACH 1: recorre proyectos activos ─────────
        for (Proyecto proyectoActivo : proyectosActivos) {

            List<Sprint> sprintsActivos =
                sprintRepository.findByProyectoId(proyectoActivo.getId());

            // ── FOREACH 2: recorre sprints del proyecto ──
            for (Sprint sprintActual : sprintsActivos) {

                CoreResponseDTO resultado = new CoreResponseDTO();
                resultado.setNombreSprint(sprintActual.getNombre());
                resultado.setNombreProyecto(proyectoActivo.getNombre());

                // ── PASO 1: similitud con históricos ─────
                int puntaje = calcularSimilitud(sprintActual, proyectosHistoricos);
                resultado.setPorcentajeSimilitud(puntaje);
                resultado.setAlertaRiesgo(determinarAlerta(puntaje));

                // ── PASO 2: balance de carga laboral ─────
                int horasNecesarias  = calcularHorasTareas(sprintActual);
                int horasDisponibles = calcularHorasEquipo();
                int deficit          = horasNecesarias - horasDisponibles;

                resultado.setHorasNecesarias(horasNecesarias);
                resultado.setHorasDisponibles(horasDisponibles);
                resultado.setDeficit(deficit);

                // ── PASO 3: asignación de freelancer ─────
                if (deficit > UMBRAL_DEFICIT_HORAS) {

                    Freelancer asignado = buscarFreelancer(freelancers, deficit);

                    if (asignado != null) {
                        // Costo real = deficit × tarifa propia del freelancer
                        double costo = deficit * asignado.getCostoHora();

                        resultado.setFreelancerAsignado(true);
                        resultado.setNombreFreelancer(asignado.getUsuario().getNombre());
                        resultado.setEspecialidadFreelancer(asignado.getEspecialidad());
                        resultado.setHorasAsignadasFreelancer(deficit);
                        resultado.setCostoEstimadoFreelancer(costo);
                    } else {
                        resultado.setFreelancerAsignado(false);
                        resultado.setNombreFreelancer("Sin freelancer disponible");
                        resultado.setEspecialidadFreelancer("—");
                        resultado.setHorasAsignadasFreelancer(0);
                        resultado.setCostoEstimadoFreelancer(0);
                    }

                } else {
                    resultado.setFreelancerAsignado(false);
                    resultado.setNombreFreelancer(null);
                    resultado.setEspecialidadFreelancer(null);
                    resultado.setHorasAsignadasFreelancer(0);
                    resultado.setCostoEstimadoFreelancer(0);
                }

                resultados.add(resultado);
            }
        }

        return resultados;
    }

    // ── MÉTODO 1: similitud con foreach anidados ─────────
    private int calcularSimilitud(Sprint sprintActual,
                                  List<Proyecto> historicos) {
        int mejorPuntaje = 0;

        long duracionActual = sprintActual.getFechaInicio()
                .until(sprintActual.getFechaFin()).getDays();

        // FOREACH sobre proyectos históricos
        for (Proyecto proyectoHistorico : historicos) {

            List<Sprint> sprintsHistoricos =
                sprintRepository.findByProyectoId(proyectoHistorico.getId());

            // FOREACH sobre sprints históricos
            for (Sprint sprintHistorico : sprintsHistoricos) {

                int puntaje = 0;

                long duracionHistorica = sprintHistorico.getFechaInicio()
                        .until(sprintHistorico.getFechaFin()).getDays();

                // Regla 1: misma duración → +30 puntos
                if (duracionActual == duracionHistorica) puntaje += 30;

                // Regla 2: horas estimadas similares → +40 puntos
                int diferenciaHoras = Math.abs(
                    sprintActual.getHorasEstimadas() - sprintHistorico.getHorasEstimadas()
                );
                if (diferenciaHoras < 10) puntaje += 40;

                // Regla 3: sprint histórico completado → +30 puntos
                if ("COMPLETADO".equals(sprintHistorico.getEstado())) puntaje += 30;

                if (puntaje > mejorPuntaje) mejorPuntaje = puntaje;
            }
        }

        return mejorPuntaje;
    }

    // ── MÉTODO 2: nivel de alerta ─────────────────────────
    private String determinarAlerta(int puntaje) {
        if (puntaje >= 70) return "BAJO";
        if (puntaje >= 40) return "MEDIO";
        return "ALTO";
    }

    // ── MÉTODO 3: suma horas de tareas con foreach ────────
    private int calcularHorasTareas(Sprint sprint) {
        int total = 0;
        List<Tarea> tareas = tareaRepository.findBySprintId(sprint.getId());

        // FOREACH sobre tareas del sprint
        for (Tarea tarea : tareas) {
            total += tarea.getHorasNecesarias();
        }
        return total;
    }

    // ── MÉTODO 4: suma horas de developers con foreach ────
    private int calcularHorasEquipo() {
        int total = 0;
        List<Usuario> developers = usuarioRepository.findByRol("ROLE_DEVELOPER");

        // FOREACH sobre developers
        for (Usuario developer : developers) {
            total += developer.getHorasDisponibles();
        }
        return total;
    }

    // ── MÉTODO 5: busca freelancer en tabla freelancers ───
    private Freelancer buscarFreelancer(List<Freelancer> freelancers,
                                        int horasRequeridas) {
        // FOREACH sobre freelancers activos
        for (Freelancer freelancer : freelancers) {
            if (freelancer.getHorasDisponibles() >= horasRequeridas) {
                return freelancer;
            }
        }
        return null;
    }
}