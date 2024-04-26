package nl.dtls.fairdatapoint.service.seeder.entity;

import lombok.Data;
import nl.dtls.fairdatapoint.entity.user.UserRole;

import java.util.UUID;

@Data
public class UserDSO {

    private UUID uuid = UUID.randomUUID();

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    private UserRole role = UserRole.USER;
}
