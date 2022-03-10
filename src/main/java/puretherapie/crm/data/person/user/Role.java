package puretherapie.crm.data.person.user;

import lombok.*;
import puretherapie.crm.data.historical.HistoricalLevel;

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
@Table(name = "Role")
public class Role implements Serializable {

    // Constants.

    public static final String BOSS_ROLE = "ROLE_BOSS";
    public static final String SECRETARY_ROLE = "ROLE_SECRETARY";
    public static final String MAMY_ROLE = "ROLE_MAMY";
    public static final String TECHNICIAN_ROLE = "ROLE_TECHNICIAN";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idRole", nullable = false)
    private Integer id;

    @Column(name = "roleName", nullable = false, length = 15)
    private String roleName;

    @ManyToMany
    @JoinTable(name = "AssociationRoleHistoricalLevel",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idHistoricalLevel"))
    @ToString.Exclude
    private List<HistoricalLevel> historicalLevels;

    @ManyToMany
    @JoinTable(name = "AssociationRoleAuthority",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idAuthority"))
    @ToString.Exclude
    private List<Authority> authorities;

    @ManyToMany
    @JoinTable(name = "AssociationUserRole",
            joinColumns = @JoinColumn(name = "idRole"),
            inverseJoinColumns = @JoinColumn(name = "idPerson"))
    @ToString.Exclude
    private List<User> users;
}