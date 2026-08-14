package com.luisramirez.booknest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BooknestApiTests {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"%s"}
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    private long createAuthor(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/authors")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"%s","bio":"bio de prueba"}
                    """.formatted(name)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private long createBook(String token, String title, String isbn, long authorId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"%s","isbn":"%s","publishedYear":2000,"authorId":%d,"totalCopies":2}
                    """.formatted(title, isbn, authorId)))
            .andExpect(status().isCreated())
            .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void borrow(String token, long bookId, int expectedStatus) throws Exception {
        mockMvc.perform(post("/api/loans")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":" + bookId + "}"))
            .andExpect(status().is(expectedStatus));
    }

    @Test
    void registerLoginAndAccessPublicCatalog() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Nuevo Lector","email":"nuevo@example.com","password":"secreto123"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.role").value("USER"));

        mockMvc.perform(get("/api/books"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void catalogSearchIsPublic() throws Exception {
        mockMvc.perform(get("/api/books").param("q", "soledad"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Cien años de soledad"));
    }

    @Test
    void userCannotCreateBooks() throws Exception {
        String userToken = login("user@booknest.dev", "user123");

        mockMvc.perform(post("/api/books")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Intruso","isbn":"978-0000000001","publishedYear":2020,"authorId":1,"totalCopies":1}
                    """))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCreatesBookAndUserBorrowsAndReturns() throws Exception {
        String adminToken = login("admin@booknest.dev", "admin123");
        long authorId = createAuthor(adminToken, "Autor Test " + System.nanoTime());
        long bookId = createBook(adminToken, "Libro Test", "978-" + System.nanoTime(), authorId);

        String userToken = login("user@booknest.dev", "user123");

        MvcResult borrow = mockMvc.perform(post("/api/loans")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":" + bookId + "}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.active").value(true))
            .andReturn();
        long loanId = objectMapper.readTree(borrow.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/loans/me")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(loanId));

        mockMvc.perform(put("/api/loans/" + loanId + "/return")
                .header("Authorization", "Bearer " + userToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/books/" + bookId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availableCopies").value(2));
    }

    @Test
    void duplicateBorrowIsConflict() throws Exception {
        String adminToken = login("admin@booknest.dev", "admin123");
        long authorId = createAuthor(adminToken, "Autor Dup " + System.nanoTime());
        long bookId = createBook(adminToken, "Libro Dup", "978-" + System.nanoTime(), authorId);

        String userToken = login("user@booknest.dev", "user123");

        borrow(userToken, bookId, 201);
        borrow(userToken, bookId, 409);
    }

    @Test
    void unauthenticatedRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"bookId\":1}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void validationErrorsReturn400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"","email":"no-es-email","password":"123"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors").exists());
    }
}
