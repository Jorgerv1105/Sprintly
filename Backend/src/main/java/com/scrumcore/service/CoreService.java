package com.scrumcore.service;

import com.scrumcore.dto.CapacidadResponseDTO;
import com.scrumcore.dto.ContratacionDTO;
import com.scrumcore.dto.CoreResponseDTO;
import com.scrumcore.dto.DetalleHistoricoCapacidadDTO;
import com.scrumcore.dto.EquipoCapacidadDTO;
import com.scrumcore.dto.EquipoInternoDTO;
import com.scrumcore.dto.SimuladorResponseDTO;
import com.scrumcore.dto.TareaPredichaDTO;
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

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CoreService {

    private final ProyectoRepository proyectoRepository;
    private final SprintRepository sprintRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FreelancerRepository freelancerRepository; // Añadido para el Paso 5 de analizarCapacidad

    public CoreService(
            ProyectoRepository proyectoRepository,
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

    // ── ANALIZA TODOS LOS SPRINTS ─────────────────────────────────────────────
    public List<CoreResponseDTO> analizarSprints() {

        List<CoreResponseDTO> resultados = new ArrayList<>();
        List<Proyecto> proyectos = proyectoRepository.findAll();

        // FOREACH 1
        for (Proyecto proyecto : proyectos) {
            List<Sprint> sprints = sprintRepository.findByProyectoId(proyecto.getId());

            // FOREACH 2
            for (Sprint sprint : sprints) {
                CoreResponseDTO resultado = analizarSprintInterno(sprint);
                resultado.setNombreProyecto(proyecto.getNombre());
                resultados.add(resultado);
            }
        }

        return resultados;
    }

    // ── ANALIZA UN SOLO SPRINT ────────────────────────────────────────────────
    public CoreResponseDTO analizarSprintIndividual(Long sprintId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint no encontrado"));

        return analizarSprintInterno(sprint);
    }

    // ── LÓGICA PRINCIPAL ──────────────────────────────────────────────────────
    private CoreResponseDTO analizarSprintInterno(Sprint sprint) {

        CoreResponseDTO dto = new CoreResponseDTO();

        dto.setNombreSprint(sprint.getNombre());
        dto.setNombreProyecto(sprint.getProyecto().getNombre());

        List<Tarea> tareas = tareaRepository.findBySprintId(sprint.getId());

        int horasNecesarias = 0;

        // FOREACH 3
        for (Tarea tarea : tareas) {
            horasNecesarias += tarea.getHorasNecesarias();
        }

        List<Usuario> developers = usuarioRepository.findByRol("ROLE_DEVELOPER");

        int horasDisponibles = 0;

        List<EquipoInternoDTO> equipoInterno = new ArrayList<>();

        // FOREACH 4
        for (Usuario developer : developers) {

            horasDisponibles += developer.getHorasDisponibles();

            equipoInterno.add(
                    new EquipoInternoDTO(
                            developer.getNombre(),
                            developer.getRol(),
                            developer.getHorasDisponibles()
                    )
            );
        }

        int deficit = horasNecesarias - horasDisponibles;

        dto.setHorasNecesarias(horasNecesarias);
        dto.setHorasDisponibles(horasDisponibles);
        dto.setDeficit(Math.max(deficit, 0));

        if (deficit > 0) {
            dto.setViabilidad("DEFICITARIO");
        } else {
            dto.setViabilidad("VIABLE");
        }

        dto.setEquipoInterno(equipoInterno);

        return dto;
    }

    // ── CAPACITY CORE ─────────────────────────────────────────────────────────
    // Equivalente al capacityController.js del proyecto udla pero en Java puro
    public CapacidadResponseDTO analizarCapacidad(Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint no encontrado"));

        CapacidadResponseDTO response = new CapacidadResponseDTO();
        response.setNombreSprint(sprint.getNombre());
        response.setNombreProyecto(sprint.getProyecto().getNombre());

        // ── PASO 1: calcular días hábiles del sprint ──────────────────────────
        long diasHabiles = calcularDiasHabiles(sprint.getFechaInicio(), sprint.getFechaFin());
        double semanas = Math.round((diasHabiles / 5.0) * 10.0) / 10.0;

        response.setDiasHabiles(diasHabiles);
        response.setSemanas(semanas);

        // ── PASO 2: foreach sobre proyectos históricos ────────────────────────
        List<Proyecto> historicos = proyectoRepository.findByEstado("HISTORICO");
        double totalHorasHist = 0;
        List<DetalleHistoricoCapacidadDTO> detalleHistorico = new ArrayList<>();

        for (Proyecto pHist : historicos) {
            List<Sprint> sprintsHist = sprintRepository.findByProyectoId(pHist.getId());
            double horasProyecto = 0;

            // foreach sobre sprints del proyecto histórico
            for (Sprint sHist : sprintsHist) {
                List<Tarea> tareasHist = tareaRepository.findBySprintId(sHist.getId());

                // foreach sobre tareas completadas del sprint histórico
                for (Tarea t : tareasHist) {
                    if ("COMPLETADA".equals(t.getEstado())) {
                        horasProyecto += t.getHorasNecesarias();
                    }
                }
            }

            totalHorasHist += horasProyecto;
            detalleHistorico.add(
                new DetalleHistoricoCapacidadDTO(pHist.getNombre(), horasProyecto)
            );
        }

        // Promedio histórico = base de estimación
        double horasEstimadas = historicos.isEmpty() ? 0
                : Math.round((totalHorasHist / historicos.size()) * 100.0) / 100.0;

        response.setProyectosHistoricosAnalizados(historicos.size());
        response.setDetalleHistorico(detalleHistorico);
        response.setHorasEstimadas(horasEstimadas);

        // ── PASO 3: foreach sobre developers con máx 8h/día ──────────────────
        List<Usuario> developers = usuarioRepository.findByRol("ROLE_DEVELOPER");
        List<EquipoCapacidadDTO> equipo = new ArrayList<>();
        double horasInternas = 0;

        for (Usuario dev : developers) {
            // Horas por día = horasDisponibles semanales / 5, máximo 8h
            double horasDia = Math.min(dev.getHorasDisponibles() / 5.0, 8.0);
            double horasDisp = Math.round(horasDia * diasHabiles * 100.0) / 100.0;
            
            horasInternas += horasDisp;
            
            equipo.add(new EquipoCapacidadDTO(
                dev.getNombre(),
                Math.round(horasDia * 100.0) / 100.0,
                horasDisp
            ));
        }

        horasInternas = Math.round(horasInternas * 100.0) / 100.0;
        response.setHorasInternas(horasInternas);
        response.setEquipoInterno(equipo);

        // ── PASO 4: calcular déficit ──────────────────────────────────────────
        double deficit = Math.round((horasEstimadas - horasInternas) * 100.0) / 100.0;
        boolean hayDeficit = deficit > 0;
        double riesgoOp = 0;

        if (hayDeficit && horasEstimadas > 0) {
            riesgoOp = Math.round((deficit / horasEstimadas) * 100 * 100.0) / 100.0;
        }

        response.setDeficit(hayDeficit ? deficit : 0);
        response.setHayDeficit(hayDeficit);
        response.setRiesgoOperativo(riesgoOp);
        response.setViabilidad(hayDeficit ? "Deficitario" : "Viable");

        // ── PASO 5: foreach sobre freelancers para cubrir el déficit ─────────
        List<ContratacionDTO> contrataciones = new ArrayList<>();
        double costoTotal = 0;

        if (hayDeficit) {
            List<Freelancer> freelancers = freelancerRepository.findByActivoTrue();
            // Ordenar por tarifa más barata primero
            freelancers.sort(Comparator.comparingDouble(Freelancer::getCostoHora));

            double horasFaltantes = deficit;

            for (Freelancer f : freelancers) {
                if (horasFaltantes <= 0) break;

                int horasAsignar = Math.min(f.getHorasDisponibles(), (int) Math.ceil(horasFaltantes));
                double costo = Math.round(horasAsignar * f.getCostoHora() * 100.0) / 100.0;

                contrataciones.add(new ContratacionDTO(
                    f.getUsuario().getNombre(),
                    f.getEspecialidad(),
                    f.getCostoHora(),
                    horasAsignar,
                    costo
                ));

                costoTotal += costo;
                horasFaltantes -= horasAsignar;
            }
        }

        response.setCostoExtraTotal(Math.round(costoTotal * 100.0) / 100.0);
        response.setContratacionesSugeridas(contrataciones);

        return response;
    }

    // ── SIMULADOR ─────────────────────────────────────────────────────────────
    // Equivalente al sprintPredictController.js del proyecto udla
    public SimuladorResponseDTO simularSprint(Long sprintId,
                                              String nombreNuevaTarea,
                                              Integer horasNuevaTarea) {
                                              
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint no encontrado"));

        List<Tarea> tareasDelSprint = tareaRepository.findBySprintId(sprintId);
        List<Usuario> developers = usuarioRepository.findByRol("ROLE_DEVELOPER");
        List<Tarea> todasLasTareas = tareaRepository.findAll();

        // Capacidad del sprint = suma de horas disponibles de todos los developers
        double sprintCapacity = 0;
        for (Usuario dev : developers) {
            sprintCapacity += dev.getHorasDisponibles();
        }

        List<TareaPredichaDTO> resultados = new ArrayList<>();
        double totalHorasPredichas = 0;

        // foreach sobre tareas actuales del sprint
        for (Tarea tarea : tareasDelSprint) {
            TareaPredichaDTO predicha = predecirTarea(
                tarea.getNombre(), tarea.getHorasNecesarias(), sprintId, todasLasTareas
            );
            predicha.setEsNueva(false);
            resultados.add(predicha);
            totalHorasPredichas += predicha.getHorasPredichas();
        }

        // Agregar tarea simulada si el usuario la proporcionó
        if (nombreNuevaTarea != null && !nombreNuevaTarea.isBlank()) {
            int horas = horasNuevaTarea != null ? horasNuevaTarea : 8;
            TareaPredichaDTO predicha = predecirTarea(
                nombreNuevaTarea, horas, sprintId, todasLasTareas
            );
            predicha.setEsNueva(true);
            resultados.add(predicha);
            totalHorasPredichas += predicha.getHorasPredichas();
        }

        totalHorasPredichas = Math.round(totalHorasPredichas * 100.0) / 100.0;

        // Calcular riesgo del sprint
        String sprintRisk = "Bajo";
        if (totalHorasPredichas > sprintCapacity) sprintRisk = "Medio";
        if (totalHorasPredichas > sprintCapacity * 1.3) sprintRisk = "Alto";

        SimuladorResponseDTO response = new SimuladorResponseDTO();
        response.setSprint(sprint.getNombre());
        response.setProyecto(sprint.getProyecto().getNombre());
        response.setSprintCapacity(sprintCapacity);
        response.setTotalHorasPredichas(totalHorasPredichas);
        response.setSprintRisk(sprintRisk);
        response.setTareasAnalizadas(resultados.size());
        response.setResultados(resultados);

        return response;
    }

    // ── Predice horas de una tarea buscando similares por nombre ──────────────
    private TareaPredichaDTO predecirTarea(String nombre, int horasEstimadas,
                                           Long sprintId, List<Tarea> todasLasTareas) {
                                           
        // Extraer palabras clave significativas (más de 3 caracteres)
        List<String> keywords = Arrays.stream(nombre.split(" "))
                .map(String::trim)
                .filter(k -> k.length() > 3)
                .collect(Collectors.toList());

        // foreach sobre todas las tareas buscando similares en otros sprints
        List<Tarea> similares = new ArrayList<>();
        if (!keywords.isEmpty()) {
            for (Tarea t : todasLasTareas) {
                if (t.getSprint().getId().equals(sprintId)) continue;
                
                boolean esSimilar = keywords.stream().anyMatch(
                    k -> t.getNombre().toLowerCase().contains(k.toLowerCase())
                );
                
                if (esSimilar) {
                    similares.add(t);
                    if (similares.size() >= 5) break;
                }
            }
        }

        // Calcular horas predichas como promedio de las similares
        double horasPredichas = horasEstimadas;
        String confianza = "baja";
        
        if (!similares.isEmpty()) {
            double suma = 0;
            for (Tarea t : similares) suma += t.getHorasNecesarias();
            horasPredichas = Math.round((suma / similares.size()) * 100.0) / 100.0;
            confianza = similares.size() >= 3 ? "alta" : "media";
        }

        List<String> nombresSimilares = similares.stream()
                .map(t -> t.getNombre() + " (" + t.getHorasNecesarias() + "h)")
                .collect(Collectors.toList());

        TareaPredichaDTO dto = new TareaPredichaDTO();
        dto.setNombre(nombre);
        dto.setHorasEstimadas(horasEstimadas);
        dto.setHorasPredichas(horasPredichas);
        dto.setDesviacion(Math.round((horasPredichas - horasEstimadas) * 100.0) / 100.0);
        dto.setConfianza(confianza);
        dto.setEsNueva(false);
        dto.setTareasSimilares(nombresSimilares);

        return dto;
    }

    // ── Cuenta días hábiles entre dos fechas (sin sábados ni domingos) ────────
    private long calcularDiasHabiles(java.time.LocalDate inicio, java.time.LocalDate fin) {
        long diasHabiles = 0;
        java.time.LocalDate cursor = inicio;
        
        while (!cursor.isAfter(fin)) {
            DayOfWeek dia = cursor.getDayOfWeek();
            if (dia != DayOfWeek.SATURDAY && dia != DayOfWeek.SUNDAY) {
                diasHabiles++;
            }
            cursor = cursor.plusDays(1);
        }
        
        return diasHabiles;
    }
}