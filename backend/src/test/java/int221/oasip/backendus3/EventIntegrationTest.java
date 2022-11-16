package int221.oasip.backendus3;

import int221.oasip.backendus3.entities.Role;
import int221.oasip.backendus3.entities.User;
import int221.oasip.backendus3.repository.EventRepository;
import int221.oasip.backendus3.repository.UserRepository;
import int221.oasip.backendus3.services.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@TestPropertySource(locations = "classpath:application-test.properties")
public class EventIntegrationTest {

    static {
        System.setProperty("JWT_SECRET", "testtesttesttesttesttesttesttesttesttesttesttesttesttest");
        System.setProperty("AZURE_TENANT_ID", "6f4432dc-20d2-441d-b1db-ac3380ba633d");
        System.setProperty("OASIP_JWT_ISSUER_URI", "http://localhost:8080");
    }

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventService eventService;

    @Autowired
    private MockMvc mockMvc;

    @Test
//    @WithMockUser(username = "admin", password = "admin", roles = "ADMIN")
    public void testGetAllEvents() throws Exception {
        User user = new User();
        user.setName("student");
        user.setRole(Role.STUDENT);
        user.setEmail("student@mail.com");
        user.setPassword("student");
        userRepository.save(user);



        assertThat(userRepository.findAll()).isNotEmpty();
    }
}
