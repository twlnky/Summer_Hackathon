package rut.miit.tech.summer_hackathon;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import rut.miit.tech.summer_hackathon.service.user.UserService;

@SpringBootTest
@DataJpaTest
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @BeforeEach
    public void setup(){
        //
    }

}
