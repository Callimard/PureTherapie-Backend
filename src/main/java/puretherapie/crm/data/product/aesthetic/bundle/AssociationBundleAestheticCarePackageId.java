package puretherapie.crm.data.product.aesthetic.bundle;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AssociationBundleAestheticCarePackageId implements Serializable {
    @Serial
    private static final long serialVersionUID = 7217694443525809671L;

    @Column(name = "idBundle", nullable = false)
    private int idBundle;

    @Column(name = "idAestheticCarePackage", nullable = false)
    private int idAestheticCarePackage;

    @Override
    public int hashCode() {
        return Objects.hash(idBundle, idAestheticCarePackage);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        AssociationBundleAestheticCarePackageId entity = (AssociationBundleAestheticCarePackageId) o;
        return Objects.equals(this.idBundle, entity.idBundle) &&
                Objects.equals(this.idAestheticCarePackage, entity.idAestheticCarePackage);
    }
}