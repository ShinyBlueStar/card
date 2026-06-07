package com.sample.system.card.service.application.rest;

import com.sample.system.card.service.domain.ports.input.service.StatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnumController.class)
@AutoConfigureMockMvc(addFilters = false)
class EnumControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private StatusService statusService;

    @Test
    void listsRegisteredEnumNames() throws Exception {
        mvc.perform(get("/api/v1/card/enums/names"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasItems("CARDSTATUS", "CARDREQUESTSTATUS", "REASONGROUP")));
    }

    @Test
    void returnsEnumValuesWithCodeAndDescription() throws Exception {
        mvc.perform(get("/api/v1/card/enums/cardStatus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("ISSUED"))
                .andExpect(jsonPath("$.data[0].code").value(1))
                .andExpect(jsonPath("$.data[0].description").isNotEmpty());
    }

    @Test
    void unknownEnumIsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/card/enums/NOPE"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
