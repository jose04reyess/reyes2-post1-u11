package com.tienda.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class PedidoResponseDTO {

    @Schema(description = "Identificador unico del pedido", example = "1")
    private Long id;

    @Schema(description = "Correo del cliente", example = "ana@correo.com")
    private String clienteEmail;

    @Schema(description = "Tipo de cliente", example = "VIP")
    private String tipoCliente;

    @Schema(description = "Subtotal del pedido", example = "100000.00")
    private Double subtotal;

    @Schema(description = "Descuento calculado", example = "15000.00")
    private Double descuento;

    @Schema(description = "IVA calculado (19% sobre base con descuento)", example = "16150.00")
    private Double iva;

    @Schema(description = "Total final a pagar", example = "101150.00")
    private Double total;

    @Schema(description = "Estado actual del pedido", example = "CREADO")
    private String estado;

    public PedidoResponseDTO() {
    }

    public PedidoResponseDTO(Long id, String clienteEmail, String tipoCliente,
                             Double subtotal, Double descuento, Double iva,
                             Double total, String estado) {
        this.id = id;
        this.clienteEmail = clienteEmail;
        this.tipoCliente = tipoCliente;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.iva = iva;
        this.total = total;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Double getIva() {
        return iva;
    }

    public void setIva(Double iva) {
        this.iva = iva;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

