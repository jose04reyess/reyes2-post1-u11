package com.tienda.pedidos.service;

import com.tienda.pedidos.dto.PedidoRequestDTO;
import com.tienda.pedidos.dto.PedidoResponseDTO;
import com.tienda.pedidos.entity.Pedido;
import com.tienda.pedidos.exception.RecursoNoEncontradoException;
import com.tienda.pedidos.repository.PedidoRepository;
import com.tienda.pedidos.strategy.EstrategiaDescuento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PedidoServiceImpl implements PedidoService {

    private static final Logger log = LoggerFactory.getLogger(PedidoServiceImpl.class);

    private final PedidoRepository repo;
    private final NotificacionService notificador;
    private final Map<String, EstrategiaDescuento> estrategias;

    public PedidoServiceImpl(PedidoRepository repo,
                             NotificacionService notificador,
                             Map<String, EstrategiaDescuento> estrategias) {
        this.repo = repo;
        this.notificador = notificador;
        this.estrategias = estrategias;
    }

    @Override
    public PedidoResponseDTO crear(PedidoRequestDTO dto) {
        log.info("Creando pedido: clienteEmail={}, tipoCliente={}", dto.getClienteEmail(), dto.getTipoCliente());

        String tipoClienteKey = dto.getTipoCliente() != null ? dto.getTipoCliente().toUpperCase() : "REGULAR";
        EstrategiaDescuento estrategia = estrategias.getOrDefault(tipoClienteKey, estrategias.get("REGULAR"));

        double subtotal = dto.getSubtotal();
        double descuento = (estrategia != null) ? estrategia.calcularDescuento(subtotal) : 0.0;
        double baseConDescuento = subtotal - descuento;
        double iva = baseConDescuento * 0.19;
        double total = baseConDescuento + iva;

        log.info("Descuento aplicado: clienteEmail={}, tipoCliente={}, descuento={}",
                dto.getClienteEmail(), dto.getTipoCliente(), descuento);

        Pedido pedido = new Pedido();
        pedido.setClienteEmail(dto.getClienteEmail());
        pedido.setTipoCliente(dto.getTipoCliente());
        pedido.setSubtotal(subtotal);
        pedido.setDescuento(descuento);
        pedido.setIva(iva);
        pedido.setTotal(total);
        pedido.setEstado("CREADO");
        pedido.setFechaCreacion(LocalDateTime.now());
        pedido.setNotasInternas("Pedido creado automaticamente, revisar stock antes de despachar");

        Pedido guardado = repo.save(pedido);

        notificador.enviarConfirmacion(guardado.getClienteEmail(), guardado.getTotal());

        log.info("Pedido creado exitosamente con id={}", guardado.getId());

        return toResponseDTO(guardado);
    }

    @Override
    public PedidoResponseDTO buscarPorId(Long id) {
        log.debug("Buscando pedido con id={}", id);
        return repo.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> {
                    log.warn("Pedido con id={} no encontrado", id);
                    return new RecursoNoEncontradoException("Pedido", id);
                });
    }

    @Override
    public List<PedidoResponseDTO> listarTodos() {
        return repo.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private PedidoResponseDTO toResponseDTO(Pedido p) {
        return new PedidoResponseDTO(
                p.getId(),
                p.getClienteEmail(),
                p.getTipoCliente(),
                p.getSubtotal(),
                p.getDescuento(),
                p.getIva(),
                p.getTotal(),
                p.getEstado()
        );
    }
}

