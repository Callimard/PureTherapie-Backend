package puretherapie.crm.data.person.user;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Authority")
public class Authority implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idAuthority", nullable = false)
    private Integer idAuthority;

    @Column(name = "authorityName", nullable = false, length = 25)
    private String authorityName;

    @ManyToMany
    @JoinTable(name = "AssociationRoleAuthority",
            joinColumns = @JoinColumn(name = "idAuthority"),
            inverseJoinColumns = @JoinColumn(name = "idRole"))
    @ToString.Exclude
    private List<Role> roles;
}