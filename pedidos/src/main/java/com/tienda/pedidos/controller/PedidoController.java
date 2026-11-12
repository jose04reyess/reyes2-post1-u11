package com.tienda.pedidos.controller;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.exception.ApiError;
import com.tienda.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Pedidos", description = "Creacion y consulta de pedidos")
@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un nuevo pedido", description = "Registra un nuevo pedido aplicando la estrategia de descuento segun el tipo de cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente",
                    content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada invalidos",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PedidoResponseDTO crear(@Valid @RequestBody PedidoRequestDTO dto) {
        return pedidoService.crear(dto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido por ID", description = "Consulta los detalles de un pedido a partir de su identificador unico.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public PedidoResponseDTO buscarPorId(
            @Parameter(description = "ID del pedido", example = "1")
            @PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", description = "Retorna el listado completo de pedidos registrados en el sistema.")
    @ApiResponse(responseCode = "200", description = "Listado de pedidos obtenido exitosamente")
    public List<PedidoResponseDTO> listarTodos() {
        return pedidoService.listarTodos();
    }
}

