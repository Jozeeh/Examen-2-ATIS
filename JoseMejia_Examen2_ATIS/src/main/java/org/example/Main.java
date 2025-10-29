package org.example;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Enums
enum TipoAula { TEORICA, LABORATORIO, AUDITORIO }
enum TipoEvento { CONFERENCIA, TALLER, REUNION }

// Interfaz Validable
interface Validable {
    boolean validar();
    String obtenerMensajeError();
}

// Clase Aula
class Aula {
    private int id;
    private String nombre;
    private TipoAula tipo;

    public Aula(int id, String nombre, TipoAula tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public TipoAula getTipo() { return tipo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setTipo(TipoAula tipo) { this.tipo = tipo; }

    @Override
    public String toString() {
        return id + " - " + nombre + " (" + tipo + ")";
    }
}

// Clase Reserva (abstracta)
abstract class Reserva implements Validable {
    protected int id;
    protected String responsable;
    protected LocalDate fecha;
    protected LocalTime horaInicio;
    protected LocalTime horaFin;
    protected Estado estado;
    protected Aula aula;

    public enum Estado { ACTIVA, CANCELADA, HISTORICA }

    public Reserva(int id, String responsable, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Aula aula) {
        this.id = id;
        this.responsable = responsable;
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.estado = Estado.ACTIVA;
        this.aula = aula;
    }

    public int getId() { return id; }
    public String getResponsable() { return responsable; }
    public LocalDate getFecha() { return fecha; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public Estado getEstado() { return estado; }
    public Aula getAula() { return aula; }

    public void setEstado(Estado estado) { this.estado = estado; }

    public boolean validar() {
        return !(horaInicio.isAfter(horaFin) || horaInicio.equals(horaFin));
    }

    public String obtenerMensajeError() {
        return "Horario de reserva inválido.";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s - %s (%s a %s) - Aula: %s - Estado: %s",
                id, responsable, fecha, horaInicio, horaFin, aula.getNombre(), estado);
    }
}

// Subclases de Reserva
class ReservaClase extends Reserva {
    public ReservaClase(int id, String responsable, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Aula aula) {
        super(id, responsable, fecha, horaInicio, horaFin, aula);
    }

    @Override
    public boolean validar() {
        return super.validar() && aula.getTipo() != TipoAula.AUDITORIO;
    }

    @Override
    public String obtenerMensajeError() {
        if (!super.validar()) return super.obtenerMensajeError();
        return "No se puede reservar una clase en un auditorio.";
    }
}

class ReservaPractica extends Reserva {
    public ReservaPractica(int id, String responsable, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Aula aula) {
        super(id, responsable, fecha, horaInicio, horaFin, aula);
    }

    @Override
    public boolean validar() {
        return super.validar() && aula.getTipo() != TipoAula.TEORICA;
    }

    @Override
    public String obtenerMensajeError() {
        if (!super.validar()) return super.obtenerMensajeError();
        return "No se puede reservar una práctica en un aula teórica.";
    }
}

class ReservaEvento extends Reserva {
    private TipoEvento tipoEvento;

    public ReservaEvento(int id, String responsable, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin, Aula aula, TipoEvento tipoEvento) {
        super(id, responsable, fecha, horaInicio, horaFin, aula);
        this.tipoEvento = tipoEvento;
    }

    public TipoEvento getTipoEvento() { return tipoEvento; }

    @Override
    public String toString() {
        return super.toString() + " - Evento: " + tipoEvento;
    }
}

// Excepción personalizada
class ReservaException extends Exception {
    public ReservaException(String mensaje) {
        super(mensaje);
    }
}

// Clase principal
public class Main {
    private static final List<Aula> aulas = new ArrayList<>();
    private static final List<Reserva> reservas = new ArrayList<>();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        inicializarDatos();
        mostrarMenu();
    }

    private static void inicializarDatos() {
        aulas.add(new Aula(1, "Aula 1", TipoAula.TEORICA));
        aulas.add(new Aula(2, "Aula 2", TipoAula.LABORATORIO));
        aulas.add(new Aula(3, "Auditorio Principal", TipoAula.AUDITORIO));
    }

    // ================== MENÚ PRINCIPAL ==================
    private static void mostrarMenu() {
        int opcion;
        do {
            System.out.println("\n=== SISTEMA DE RESERVAS ITCA ===");
            System.out.println("1. Gestión de aulas");
            System.out.println("2. Registro de reservas");
            System.out.println("3. Listar reservas");
            System.out.println("4. Cancelar reserva");
            System.out.println("5. Salir");
            System.out.print("Seleccione una opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> gestionAulas();
                case 2 -> registroReservas();
                case 3 -> listarReservas();
                case 4 -> cancelarReservas();
                case 5 -> System.out.println("Saliendo del sistema...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 5);
    }

    // ================== GESTIÓN DE AULAS ==================
    private static void gestionAulas() {
        int opcion;
        do {
            System.out.println("\n=== GESTIÓN DE AULAS ===");
            System.out.println("1. Listar aulas");
            System.out.println("2. Registrar aula");
            System.out.println("3. Modificar aula");
            System.out.println("4. Volver");
            System.out.print("Seleccione una opción: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 -> listarAulas();
                case 2 -> registrarAula();
                case 3 -> modificarAula();
                case 4 -> System.out.println("Volviendo al menú principal...");
                default -> System.out.println("Opción inválida.");
            }
        } while (opcion != 4);
    }

    private static void listarAulas() {
        System.out.println("\n=== LISTA DE AULAS ===");
        aulas.forEach(System.out::println);
    }

    private static void registrarAula() {
        System.out.print("Ingrese nombre del aula: ");
        String nombre = scanner.nextLine();
        System.out.println("Seleccione tipo de aula:");
        for (TipoAula t : TipoAula.values()) {
            System.out.println((t.ordinal() + 1) + ". " + t);
        }
        int tipo = leerEntero();
        TipoAula tipoAula = TipoAula.values()[tipo - 1];
        aulas.add(new Aula(aulas.size() + 1, nombre, tipoAula));
        System.out.println("Aula registrada exitosamente.");
    }

    private static void modificarAula() {
        listarAulas();
        System.out.print("Ingrese ID del aula a modificar: ");
        int id = leerEntero();
        Aula aula = aulas.stream().filter(a -> a.getId() == id).findFirst().orElse(null);

        if (aula == null) {
            System.out.println("Aula no encontrada.");
            return;
        }

        System.out.print("Nuevo nombre (actual: " + aula.getNombre() + "): ");
        String nuevoNombre = scanner.nextLine();
        System.out.println("Seleccione nuevo tipo:");
        for (TipoAula t : TipoAula.values()) {
            System.out.println((t.ordinal() + 1) + ". " + t);
        }
        int tipo = leerEntero();

        aula.setNombre(nuevoNombre);
        aula.setTipo(TipoAula.values()[tipo - 1]);
        System.out.println("Aula modificada correctamente.");
    }

    // ================== REGISTRO DE RESERVAS ==================
    private static void registroReservas() {
        try {
            System.out.print("Nombre del responsable: ");
            String responsable = scanner.nextLine();

            listarAulas();
            System.out.print("Seleccione ID de aula: ");
            int idAula = leerEntero();
            Aula aula = aulas.stream().filter(a -> a.getId() == idAula).findFirst().orElseThrow(() -> new ReservaException("Aula no encontrada."));

            LocalDate fecha = leerFecha("Ingrese fecha (YYYY-MM-DD o YYYY/MM/DD): ");
            LocalTime horaInicio = leerHora("Hora inicio (HH:MM): ");
            LocalTime horaFin = leerHora("Hora fin (HH:MM): ");

            System.out.println("Tipo de reserva:");
            System.out.println("1. Clase");
            System.out.println("2. Práctica");
            System.out.println("3. Evento");
            int tipo = leerEntero();

            Reserva reserva;
            switch (tipo) {
                case 1 -> reserva = new ReservaClase(reservas.size() + 1, responsable, fecha, horaInicio, horaFin, aula);
                case 2 -> reserva = new ReservaPractica(reservas.size() + 1, responsable, fecha, horaInicio, horaFin, aula);
                case 3 -> {
                    System.out.println("Seleccione tipo de evento:");
                    for (TipoEvento t : TipoEvento.values()) {
                        System.out.println((t.ordinal() + 1) + ". " + t);
                    }
                    int tipoEventoIdx = leerEntero();
                    TipoEvento tipoEvento = TipoEvento.values()[tipoEventoIdx - 1];
                    reserva = new ReservaEvento(reservas.size() + 1, responsable, fecha, horaInicio, horaFin, aula, tipoEvento);
                }
                default -> throw new ReservaException("Tipo de reserva inválido.");
            }

            if (reserva.validar()) {
                reservas.add(reserva);
                System.out.println("Reserva registrada con éxito.");
            } else {
                throw new ReservaException(reserva.obtenerMensajeError());
            }
        } catch (ReservaException e) {
            System.out.println(e.getMessage());
        }
    }

    // ================== OTROS MÉTODOS ==================
    private static void listarReservas() {
        System.out.println("\n=== LISTA DE RESERVAS ===");
        if (reservas.isEmpty()) {
            System.out.println("No hay reservas registradas.");
        } else {
            reservas.forEach(System.out::println);
        }
    }

    private static void cancelarReservas() {
        listarReservas();
        System.out.print("Ingrese ID de la reserva a cancelar: ");
        int id = leerEntero();
        Reserva r = reservas.stream().filter(res -> res.getId() == id).findFirst().orElse(null);

        if (r == null) {
            System.out.println("Reserva no encontrada.");
            return;
        }

        r.setEstado(Reserva.Estado.CANCELADA);
        System.out.println("Reserva cancelada correctamente.");
    }

    // ================== UTILIDADES ==================
    private static int leerEntero() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.print("Entrada inválida, ingrese un número: ");
            }
        }
    }

    private static LocalDate leerFecha(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String input = scanner.nextLine();
            try {
                input = input.replace("/", "-");
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(input, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Formato de fecha inválido. Use YYYY-MM-DD o YYYY/MM/DD.");
            }
        }
    }

    private static LocalTime leerHora(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            try {
                return LocalTime.parse(scanner.nextLine());
            } catch (DateTimeParseException e) {
                System.out.println("Formato inválido. Use HH:MM (ejemplo: 14:30).");
            }
        }
    }
}
