package com.scrumcore.service;

import com.scrumcore.dto.CoreResponseDTO;
import com.scrumcore.dto.DetalleHistoricoDTO;
import com.scrumcore.dto.EquipoInternoDTO;
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

    private final ProyectoRepository proyectoRepository;
    private final SprintRepository sprintRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FreelancerRepository freelancerRepository;

    private static final int UMBRAL_DEFICIT_HORAS = 15;

    public CoreService(ProyectoRepository proyectoRepository,
                       SprintRepository sprintRepository,
                       TareaRepository tareaRepository,
                       UsuarioRepository usuarioRepository,
                       FreelancerRepository freelancerRepository) {
        this.proyectoRepository = proyectoRepository;
        this.sprintRepository = sprintRepository;
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
        this.freelancerRepository = freelancerRepository;
    }

    // ── MÉTODO PRINCIPAL: Analizar todos los sprints activos ──
    public List<CoreResponseDTO> analizarSprints() {

        List<CoreResponseDTO> resultados = new ArrayList<>();

        List<Proyecto> proyectosActivos = proyectoRepository.findByEstado("ACTIVO");
        List<Proyecto> proyectosHistoricos = proyectoRepository.findByEstado("HISTORICO");
        List<Freelancer> freelancers = freelancerRepository.findByActivoTrue();
        List<Usuario> developers = usuarioRepository.findByRol("ROLE_DEVELOPER");

        // FOREACH 1: recorre proyectos activos
        for (Proyecto proyectoActivo : proyectosActivos) {

            List<Sprint> sprintsActivos = sprintRepository.findByProyectoId(proyectoActivo.getId());

            // FOREACH 2: recorre sprints del proyecto activo
            for (Sprint sprintActual : sprintsActivos) {

                CoreResponseDTO resultado = new CoreResponseDTO();
                resultado.setNombreSprint(sprintActual.getNombre());
                resultado.setNombreProyecto(proyectoActivo.getNombre());

                // ── PASO 1: similitud con históricos ─────
                ResultadoSimilitud similitud = calcularSimilitud(sprintActual, proyectosHistoricos);

                resultado.setPorcentajeSimilitud(similitud.puntaje);
                resultado.setAlertaRiesgo(determinarAlerta(similitud.puntaje));
                resultado.setRazonSimilitud(similitud.razon);
                resultado.setDetalleHistorico(similitud.detalle);

                // ── PASO 2: carga laboral ─────────────────
                int horasNecesarias = calcularHorasTareas(sprintActual);
                int horasDisponibles = calcularHorasEquipo(developers);
                int deficit = horasNecesarias - horasDisponibles;

                // Riesgo operativo
                double riesgoOperativo = 0;
                if (deficit > 0 && horasNecesarias > 0) {
                    riesgoOperativo = Math.round(((double) deficit / horasNecesarias) * 100 * 100.0) / 100.0;
                }

                resultado.setHorasNecesarias(horasNecesarias);
                resultado.setHorasDisponibles(horasDisponibles);
                resultado.setDeficit(deficit);
                resultado.setRiesgoOperativo(riesgoOperativo);
                resultado.setViabilidad(deficit > 0 ? "DEFICITARIO" : "VIABLE");
                resultado.setEquipoInterno(construirEquipoInterno(developers));

                // ── PASO 3: asignación de freelancer ──────
                if (deficit > UMBRAL_DEFICIT_HORAS) {
                    Freelancer asignado = buscarFreelancer(freelancers, deficit);
                    if (asignado != null) {
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

    // ── NUEVO MÉTODO: Analiza un sprint específico bajo demanda ──
    public CoreResponseDTO analizarSprintIndividual(Long sprintId) {
        
        Sprint sprintActual = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint no encontrado"));
                
        Proyecto proyecto            = sprintActual.getProyecto();
        List<Proyecto> historicos    = proyectoRepository.findByEstado("HISTORICO");
        List<Freelancer> freelancers = freelancerRepository.findByActivoTrue();
        List<Usuario> developers     = usuarioRepository.findByRol("ROLE_DEVELOPER");
        
        CoreResponseDTO resultado    = new CoreResponseDTO();
        resultado.setNombreSprint(sprintActual.getNombre());
        resultado.setNombreProyecto(proyecto.getNombre());
        
        // PASO 1: similitud
        ResultadoSimilitud similitud = calcularSimilitud(sprintActual, historicos);
        resultado.setPorcentajeSimilitud(similitud.puntaje);
        resultado.setAlertaRiesgo(determinarAlerta(similitud.puntaje));
        resultado.setRazonSimilitud(similitud.razon);
        resultado.setDetalleHistorico(similitud.detalle);
        
        // PASO 2: carga laboral
        int horasNecesarias  = calcularHorasTareas(sprintActual);
        int horasDisponibles = calcularHorasEquipo(developers);
        int deficit          = horasNecesarias - horasDisponibles;
        double riesgoOperativo = 0;
        
        if (deficit > 0 && horasNecesarias > 0) {
            riesgoOperativo = Math.round(((double) deficit / horasNecesarias) * 100 * 100.0) / 100.0;
        }
        
        resultado.setHorasNecesarias(horasNecesarias);
        resultado.setHorasDisponibles(horasDisponibles);
        resultado.setDeficit(deficit);
        resultado.setRiesgoOperativo(riesgoOperativo);
        resultado.setViabilidad(deficit > 0 ? "DEFICITARIO" : "VIABLE");
        resultado.setEquipoInterno(construirEquipoInterno(developers));
        
        // PASO 3: freelancer
        if (deficit > UMBRAL_DEFICIT_HORAS) {
            Freelancer asignado = buscarFreelancer(freelancers, deficit);
            if (asignado != null) {
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
        
        return resultado;
    }

    // ── CLASE INTERNA para resultado de similitud ─────────
    private static class ResultadoSimilitud {
        int puntaje;
        String razon;
        List<DetalleHistoricoDTO> detalle;

        ResultadoSimilitud(int puntaje, String razon, List<DetalleHistoricoDTO> detalle) {
            this.puntaje = puntaje;
            this.razon   = razon;
            this.detalle = detalle;
        }
    }

    // ── MÉTODO 1: similitud con detalle de cada comparación ─
    private ResultadoSimilitud calcularSimilitud(Sprint sprintActual, List<Proyecto> historicos) {
        int mejorPuntaje = 0;
        String mejorRazon = "Sin proyectos históricos para comparar";
        List<DetalleHistoricoDTO> detalles = new ArrayList<>();

        long duracionActual = sprintActual.getFechaInicio()
                .until(sprintActual.getFechaFin()).getDays();

        List<Tarea> tareasActuales = tareaRepository.findBySprintId(sprintActual.getId());

        // FOREACH 3: recorre proyectos históricos
        for (Proyecto proyectoHistorico : historicos) {

            List<Sprint> sprintsHistoricos = sprintRepository.findByProyectoId(proyectoHistorico.getId());

            // FOREACH 4: recorre sprints históricos
            for (Sprint sprintHistorico : sprintsHistoricos) {

                int puntaje = 0;
                List<String> reglas = new ArrayList<>();

                long duracionHistorica = sprintHistorico.getFechaInicio()
                        .until(sprintHistorico.getFechaFin()).getDays();

                // Regla 1: misma duración → +20 puntos
                if (duracionActual == duracionHistorica) {
                    puntaje += 20;
                    reglas.add("Misma duración (" + duracionActual + " días) +20pts");
                }

                // Regla 2: horas estimadas similares → +20 puntos
                int difHoras = Math.abs(sprintActual.getHorasEstimadas() - sprintHistorico.getHorasEstimadas());
                if (difHoras < 10) {
                    puntaje += 20;
                    reglas.add("Horas similares (diferencia: " + difHoras + "h) +20pts");
                }

                // Regla 3: sprint histórico completado → +20 puntos
                if ("COMPLETADO".equals(sprintHistorico.getEstado())) {
                    puntaje += 20;
                    reglas.add("Sprint histórico completado exitosamente +20pts");
                }

                // Regla 4 y 5: comparación de tareas con foreach anidado
                List<Tarea> tareasHistoricas = tareaRepository.findBySprintId(sprintHistorico.getId());

                int tareasCoincidentes = 0;

                // FOREACH 5: tareas actuales
                for (Tarea tareaActual : tareasActuales) {

                    // FOREACH 6: tareas históricas
                    for (Tarea tareaHistorica : tareasHistoricas) {
                        int difHorasTarea = Math.abs(tareaActual.getHorasNecesarias() - tareaHistorica.getHorasNecesarias());
                        if (difHorasTarea <= 5) {
                            tareasCoincidentes++;
                            break;
                        }
                    }
                }

                // Regla 4: tareas similares → hasta +20 puntos proporcionales
                if (!tareasActuales.isEmpty()) {
                    double porcentajeTareas = (double) tareasCoincidentes / tareasActuales.size();
                    int puntosTareas = (int) (porcentajeTareas * 20);
                    if (puntosTareas > 0) {
                        puntaje += puntosTareas;
                        reglas.add(tareasCoincidentes + "/" + tareasActuales.size()
                                + " tareas similares en horas +" + puntosTareas + "pts");
                    }
                }

                // Regla 5: misma cantidad de tareas → +10 puntos
                if (tareasActuales.size() == tareasHistoricas.size() && !tareasActuales.isEmpty()) {
                    puntaje += 10;
                    reglas.add("Misma cantidad de tareas (" + tareasActuales.size() + ") +10pts");
                }

                // Guardar detalle de esta comparación
                detalles.add(new DetalleHistoricoDTO(
                        proyectoHistorico.getNombre(),
                        sprintHistorico.getNombre(),
                        Math.min(puntaje, 100),
                        reglas
                ));

                if (puntaje > mejorPuntaje) {
                    mejorPuntaje = puntaje;
                    mejorRazon   = String.join(" | ", reglas);
                }
            }
        }

        return new ResultadoSimilitud(Math.min(mejorPuntaje, 100), mejorRazon, detalles);
    }

    // ── MÉTODO 2: nivel de alerta ─────────────────────────
    private String determinarAlerta(int puntaje) {
        if (puntaje >= 70) return "BAJO";
        if (puntaje >= 40) return "MEDIO";
        return "ALTO";
    }

    // ── MÉTODO 3: suma horas de tareas — FOREACH 7 ───────
    private int calcularHorasTareas(Sprint sprint) {
        int total = 0;
        List<Tarea> tareas = tareaRepository.findBySprintId(sprint.getId());
        for (Tarea tarea : tareas) {
            total += tarea.getHorasNecesarias();
        }
        return total;
    }

    // ── MÉTODO 4: suma horas de equipo — FOREACH 8 ───────
    private int calcularHorasEquipo(List<Usuario> developers) {
        int total = 0;
        for (Usuario developer : developers) {
            total += developer.getHorasDisponibles();
        }
        return total;
    }

    // ── MÉTODO 5: construye detalle del equipo ────────────
    private List<EquipoInternoDTO> construirEquipoInterno(List<Usuario> developers) {
        List<EquipoInternoDTO> equipo = new ArrayList<>();
        // FOREACH 9: detalla cada developer
        for (Usuario developer : developers) {
            equipo.add(new EquipoInternoDTO(
                    developer.getNombre(),
                    developer.getRol().replace("ROLE_", ""),
                    developer.getHorasDisponibles()
            ));
        }
        return equipo;
    }

    // ── MÉTODO 6: busca freelancer disponible — FOREACH 10 ─
    private Freelancer buscarFreelancer(List<Freelancer> freelancers, int horasRequeridas) {
        for (Freelancer freelancer : freelancers) {
            if (freelancer.getHorasDisponibles() >= horasRequeridas) {
                return freelancer;
            }
        }
        return null;
    }
}