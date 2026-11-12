package com.tienda.pedidos.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public class ApiError {

    @Schema(description = "Codigo de estado HTTP", example = "404")
    private int status;

    @Schema(description = "Descripcion del error HTTP", example = "Not Found")
    private String error;

    @Schema(description = "Mensaje descriptivo del error", example = "Pedido con id 99 no encontrado.")
    private String mensaje;

    @Schema(description = "Fecha y hora del incidente", example = "2026-11-12T10:30:00")
    private String timestamp;

    @Schema(description = "Ruta del recurso solicitado", example = "/api/pedidos/99")
    private String path;

    public ApiError() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public ApiError(int status, String error, String mensaje, String path) {
        this.status = status;
        this.error = error;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}

