package com.papeleriamatrix.dto;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RangoFechasRequest(
        @NotNull(message = "La fecha inicial es obligatoria") LocalDate desde,
        @NotNull(message = "La fecha final es obligatoria") LocalDate hasta) {

    @AssertTrue(message = "La fecha final no puede ser anterior a la inicial")
    public boolean isRangoValido() {
        return desde == null || hasta == null || !hasta.isBefore(desde);
    }
}