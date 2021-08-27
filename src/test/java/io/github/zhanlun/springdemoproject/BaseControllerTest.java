package io.github.zhanlun.springdemoproject;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BaseControllerTest {
    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions get(String url) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.get(url))
                .andDo(print());
    }

    protected ResultActions getNotFound(String url) throws Exception {
        return this.get(url)
                .andExpect(status().isNotFound());
    }

    protected ResultActions getBadRequest(String url) throws Exception {
        return this.get(url)
                .andExpect(status().isBadRequest());
    }

    protected ResultActions getForbidden(String url) throws Exception {
        return this.get(url)
                .andExpect(status().isForbidden());
    }

    protected ResultActions getOk(String url) throws Exception {
        return this.get(url)
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    protected ResultActions getOkAndJsonMatch(String url, String json) throws Exception {
        return this.getOk(url)
                .andExpect(MockMvcResultMatchers.content().json(json, false));
    }

    protected ResultActions post(String url, String json) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.post(url)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    protected ResultActions postForbidden(String url, String json) throws Exception {
        return this.post(url, json)
                .andExpect(status().isForbidden());
    }

    protected ResultActions postOk(String url, String json) throws Exception {
        return this.post(url, json)
                .andExpect(status().isCreated());
    }

    protected ResultActions delete(String url, String json) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.delete(url))
                .andDo(print());
    }

    protected ResultActions deleteForbidden(String url, String json) throws Exception {
        return this.delete(url, json)
                .andExpect(status().isForbidden());
    }

    protected ResultActions deleteOk(String url, String json) throws Exception {
        return this.delete(url, json)
                .andExpect(status().isOk());
    }

    protected ResultActions patch(String url, String json) throws Exception {
        return this.mockMvc.perform(MockMvcRequestBuilders.patch(url)
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    protected ResultActions patchOk(String url, String json) throws Exception {
        return this.patch(url, json)
                .andExpect(status().isOk());
    }
}
