package cl.duoc.productos;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductoControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void listarProductos() throws Exception {
        mvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void buscarProductoExistente() throws Exception {
        mvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Teclado mecanico"));
    }

    @Test
    void buscarProductoInexistente() throws Exception {
        mvc.perform(get("/api/productos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearProductoValido() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Webcam\",\"precio\":24990,\"stock\":4}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4));
    }

    @Test
    void crearProductoInvalidoDevuelve400() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"\",\"precio\":-1,\"stock\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void descontarStock() throws Exception {
        mvc.perform(post("/api/productos/2/descontar").param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void descontarStockInsuficienteDevuelve409() throws Exception {
        mvc.perform(post("/api/productos/3/descontar").param("cantidad", "100"))
                .andExpect(status().isConflict());
    }

    @Test
    void descontarProductoInexistenteDevuelve404() throws Exception {
        mvc.perform(post("/api/productos/99/descontar").param("cantidad", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthCheckEstaArriba() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
