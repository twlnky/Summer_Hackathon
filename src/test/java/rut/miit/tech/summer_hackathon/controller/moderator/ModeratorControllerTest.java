package rut.miit.tech.summer_hackathon.controller.moderator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import rut.miit.tech.summer_hackathon.domain.dto.ModeratorDTO;
import rut.miit.tech.summer_hackathon.domain.model.Moderator;
import rut.miit.tech.summer_hackathon.service.moderator.ModeratorService;
import rut.miit.tech.summer_hackathon.service.util.PageResult;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ModeratorController.class)
class ModeratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModeratorService moderatorService;

    private Moderator moderator;
    private ModeratorDTO moderatorDTO;

    @BeforeEach
    void setup() {
        moderator = new Moderator(
                1L,
                "moderator1",
                "Valid123!",
                null,
                null,
                null,
                List.of()
        );

        moderatorDTO = new ModeratorDTO(
                1L,
                "moderator1",
                "Valid123!",
                List.of(1L)
        );
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getAll_shouldReturnListForAdmin() throws Exception {
        PageResult<Moderator> pageResult = new PageResult<>(
                List.of(moderator),
                1,
                10,
                1L
        );
        when(moderatorService.getAll(any(), any())).thenReturn(pageResult);

        mockMvc.perform(get("/api/v1/moderators"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.queryResult[0].login").value("moderator1"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void getById_shouldReturnModerator() throws Exception {
        when(moderatorService.getById(1L)).thenReturn(moderator);

        mockMvc.perform(get("/api/v1/moderators/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("moderator1"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void create_shouldCreateModerator() throws Exception {
        when(moderatorService.save(any())).thenReturn(moderator);

        String json = """
                {
                  "id": 1,
                  "login": "moderator1",
                  "password": "Valid123!",
                  "departmentsIds": [1,2]
                }
                """;

        mockMvc.perform(post("/api/v1/moderators")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("moderator1"));
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void delete_shouldDeleteModerator() throws Exception {
        when(moderatorService.getById(1L)).thenReturn(moderator);
        doNothing().when(moderatorService).delete(eq(1L));

        mockMvc.perform(delete("/api/v1/moderators/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
