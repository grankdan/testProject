package com.task.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.task.test.data.Product;
import com.task.test.data.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ProductController.class)
class TestApplicationTests {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @MockBean
    private ProductRepository productRepository;

    @Autowired
    private MockMvc mockMvc;


    private final Product testProduct = new Product(1L, "coffee", 2.3, "5/10/2020");

    @Test
    void postTest() throws Exception {
        final AtomicBoolean attemptedToSave = new AtomicBoolean(false);
        final AtomicBoolean equals = new AtomicBoolean(false);

        when(productRepository.save(any())).thenAnswer((Answer<Product>) invocation -> {
            attemptedToSave.set(true);
            final Product productToSave = invocation.getArgument(0);

            equals.set(testProduct.getName().equals(productToSave.getName()) &&
                    testProduct.getPrice().equals(productToSave.getPrice()) &&
                    testProduct.getDate().equals(productToSave.getDate()));
            return productToSave;
        });

        mockMvc.perform(
                post("/products")
                        .contentType("application/json")
                        .content(GSON.toJson(testProduct))
        ).andExpect(status().isOk());

        assertTrue(attemptedToSave.get(), "Test is not setup correctly, product save was not invoked");
        assertTrue(equals.get(), "Product to save fields are not matching the json sent to the server");
    }


    @Test
    void getTest() throws Exception {
        final List<Product> allProducts = List.of(this.testProduct);

        when(productRepository.findAll()).thenReturn(allProducts);

        mockMvc.perform(
                get("/products")
        ).andExpect(content().json(GSON.toJson(allProducts)));
    }
}
