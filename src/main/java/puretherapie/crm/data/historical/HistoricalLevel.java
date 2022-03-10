package puretherapie.crm.data.historical;

import lombok.*;
import puretherapie.crm.data.person.user.Role;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HistoricalLevel")
public class HistoricalLevel implements Serializable {

    // Constants.

    public static final String ALL_ROLES_LEVEL = "ALL_ROLES";
    public static final String BOSS_LEVEL = "BOSS";
    public static final String BOSS_SECRETARY_LEVEL = "BOSS_SECRETARY";
    public static final String BOSS_SECRETARY_MAMY_LEVEL = "BOSS_SECRETARY_MAMY";
    public static final String BOSS_SECRETARY_MAMY_TECHNICIAN_LEVEL = "BOSS_SECRETARY_MAMY_TECHNICIAN";

    // Variables.

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idHistoricalLevel", nullable = false)
    private Integer idHistoricalLevel;

    @Column(name = "historicalLevelName", nullable = false, length = 45)
    private String historicalLevelName;

    @ManyToMany
    @JoinTable(name = "AssociationRoleHistoricalLevel",
            joinColumns = @JoinColumn(name = "idHistoricalLevel"),
            inverseJoinColumns = @JoinColumn(name = "idRole"))
    @ToString.Exclude
    private List<Role> roles;
}