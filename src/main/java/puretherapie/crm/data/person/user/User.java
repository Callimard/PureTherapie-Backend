package puretherapie.crm.data.person.user;

import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import puretherapie.crm.data.person.Person;
import puretherapie.crm.data.person.PersonOrigin;

import javax.persistence.*;
import java.time.LocalDate;
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

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(name = "AssociationUserRole",
            joinColumns = @JoinColumn(name = "idPerson"),
            inverseJoinColumns = @JoinColumn(name = "idRole"))
    @ToString.Exclude
    private List<Role> roles;

    @Builder
    public User(Integer idPerson, String firstName, String lastName, String mail, boolean gender, LocalDate birthday, String phone,
                OffsetDateTime creationDate, PersonOrigin personOrigin, String username, String password) {
        super(idPerson, firstName, lastName, mail, gender, birthday, phone, creationDate, personOrigin);
        this.username = username;
        this.password = password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> list = new ArrayList<>();
        if (roles != null)
            roles.forEach(r -> list.add(new SimpleGrantedAuthority(r.getRoleName())));
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
