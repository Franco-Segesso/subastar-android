package com.grupo6.subastar.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class FormatoPujas {
    private static final Locale LOCALE_AR = new Locale("es", "AR");
    private static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd MMM yyyy", LOCALE_AR);
    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", LOCALE_AR);

    private FormatoPujas() {
    }

    public static String moneda(String moneda, Double importe) {
        NumberFormat formato = NumberFormat.getNumberInstance(LOCALE_AR);
        formato.setMaximumFractionDigits(0);
        formato.setMinimumFractionDigits(0);
        return (moneda == null ? "" : moneda + " ")
                + formato.format(importe == null ? 0.0 : importe);
    }

    public static String fechaHoraSubasta(String fecha, String hora) {
        try {
            LocalDate fechaLocal = LocalDate.parse(fecha);
            LocalTime horaLocal = LocalTime.parse(hora);
            return capitalizar(fechaLocal.format(FECHA)) + " · " + horaLocal.format(HORA) + " hs";
        } catch (Exception e) {
            return (fecha == null ? "" : fecha) + " · " + horaCorta(hora) + " hs";
        }
    }

    public static String fechaHoraPuja(String fechaHora) {
        try {
            return LocalDateTime.parse(fechaHora).format(FECHA_HORA);
        } catch (Exception e) {
            return fechaHora == null ? "--" : fechaHora.replace("T", " ");
        }
    }

    private static String horaCorta(String hora) {
        if (hora == null) return "--:--";
        return hora.length() >= 5 ? hora.substring(0, 5) : hora;
    }

    private static String capitalizar(String valor) {
        if (valor == null || valor.isEmpty()) return valor;
        return valor.substring(0, 1).toUpperCase(LOCALE_AR) + valor.substring(1);
    }
}
