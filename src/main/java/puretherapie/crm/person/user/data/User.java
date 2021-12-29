package puretherapie.crm.person.user.data;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import puretherapie.crm.person.Person;
import puretherapie.crm.person.PersonOrigin;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Date;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@ToString(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "User")
@DiscriminatorValue("U")
public class User extends Person implements UserDetails {

    private String username;

    private String password;

    private byte role;

    @Builder
    public User(Long idPerson, String firstName, String lastName, String mail, boolean gender, Date birthday, String phone,
                OffsetDateTime creationDate, PersonOrigin personOrigin, String username, String password) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new SimpleGrantedAuthority(UserRole.getRole(role).convertToRole()));
        return list;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum UserRole {
        BOSS(0), MAMY(1), SECRETARY(2), TECHNICIAN(3), OTHER(4);

        private final int level;

        UserRole(int level) {
            this.level = level;
        }

        public static UserRole getRole(int level) {
            return switch (level) {
                case 0 -> BOSS;
                case 1 -> MAMY;
                case 2 -> SECRETARY;
                case 3 -> TECHNICIAN;
                default -> OTHER;
            };
        }

        public String convertToRole() {
            return "ROLE_" + level;
        }

        public int getLevel() {
            return level;
        }
    }
}
